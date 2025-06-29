package org.acme;

public class UploadProgress {
    public long totalBytes;
    public long uploadedBytes;

    public UploadProgress(long totalBytes) {
        this.totalBytes = totalBytes;
        this.uploadedBytes = 0;
    }

    public int getPercentage() {
        if (totalBytes == 0)
            return 0;
        return (int) ((uploadedBytes * 100) / totalBytes);
    }
}