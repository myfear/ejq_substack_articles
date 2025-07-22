package com.example;

import java.io.InputStream;
import java.time.LocalDateTime;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class SftpService {

    @ConfigProperty(name = "sftp.host")
    String host;
    // ... (other @ConfigProperty fields for port, user, password, remoteDirectory)
    @ConfigProperty(name = "sftp.port")
    int port;

    @ConfigProperty(name = "sftp.user")
    String user;

    @ConfigProperty(name = "sftp.password")
    String password;

    @ConfigProperty(name = "sftp.remote.directory")
    String remoteDirectory;

    @Transactional // This is crucial for database operations
    public FileMetadata uploadFile(String fileName, long fileSize, InputStream inputStream)
            throws JSchException, SftpException {
        // 1. Upload the file to SFTP
        ChannelSftp channelSftp = createSftpChannel();
        try {
            channelSftp.connect();
            String remoteFilePath = remoteDirectory + "/" + fileName;
            channelSftp.put(inputStream, remoteFilePath);
        } finally {
            disconnectChannel(channelSftp);
        }

        // 2. Persist metadata to the database
        FileMetadata meta = new FileMetadata();
        meta.fileName = fileName;
        meta.fileSize = fileSize;
        meta.uploadTimestamp = LocalDateTime.now();
        meta.persist(); // Panache makes saving simple!

        return meta;
    }

    public InputStream downloadFile(String fileName) throws JSchException, SftpException {
        // This method remains the same as before
        ChannelSftp channelSftp = createSftpChannel();
        channelSftp.connect();
        String remoteFilePath = remoteDirectory + "/" + fileName;
        return new SftpInputStream(channelSftp.get(remoteFilePath), channelSftp);
    }

    // The private helper methods (createSftpChannel, disconnectChannel,
    // SftpInputStream)
    // remain the same. Copy them from the previous tutorial.
    private ChannelSftp createSftpChannel() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return (ChannelSftp) session.openChannel("sftp");
    }

    private void disconnectChannel(ChannelSftp channel) {
        if (channel != null) {
            if (channel.isConnected()) {
                channel.disconnect();
            }
            try {
                if (channel.getSession() != null && channel.getSession().isConnected()) {
                    channel.getSession().disconnect();
                }
            } catch (JSchException e) {
                // Ignore
            }
        }
    }

    private class SftpInputStream extends InputStream {
        private final InputStream sftpStream;
        private final ChannelSftp channelToDisconnect;

        public SftpInputStream(InputStream sftpStream, ChannelSftp channel) {
            this.sftpStream = sftpStream;
            this.channelToDisconnect = channel;
        }

        @Override
        public int read() throws java.io.IOException {
            return sftpStream.read();
        }

        @Override
        public void close() throws java.io.IOException {
            sftpStream.close();
            disconnectChannel(channelToDisconnect);
        }
    }
}