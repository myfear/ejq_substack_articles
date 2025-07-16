package org.acme;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DnsTools {

    @Tool("Get the A records for a given domain")
    public List<String> getARecords(String domain) {
        return getRecords(domain, Type.A);
    }

    @Tool("Get the MX records for a given domain")
    public List<String> getMxRecords(String domain) {
        return getRecords(domain, Type.MX);
    }

    @Tool("Get the CNAME records for a given domain")
    public List<String> getCnameRecords(String domain) {
        return getRecords(domain, Type.CNAME);
    }

    private List<String> getRecords(String domain, int type) {
        try {
            Lookup lookup = new Lookup(domain, type);
            Record[] records = lookup.run();
            if (lookup.getResult() == Lookup.SUCCESSFUL) {
                return Arrays.stream(records)
                        .map(Record::rdataToString)
                        .collect(Collectors.toList());
            }
        } catch (TextParseException e) {
            e.printStackTrace();
        }
        return List.of();
    }
}