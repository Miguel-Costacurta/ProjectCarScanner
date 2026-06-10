package obd.core.pids;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ElmParser {
    private ElmParser() {};

    public static String[] extrairResposta41(String raw, String pid){
        if (raw == null) return new String[0];

        String u = raw.toUpperCase(Locale.ROOT);;

        if(
                u.contains("NO DATA") ||
                        u.contains("UNABLE") ||
                        u.contains("STOPPED") ||
                        u.contains("CAN ERROR") ||
                        u.contains("?")
        ){
            return new String[0];
        }

        StringBuilder hex = new StringBuilder(u.length());

        for (int i = 0; i < u.length(); i++){
            char c = u.charAt(i);
            if((c >= '0' && c <= '9') || (c >= 'A' && c<= 'F')){
                hex.append(c);
            }
        }

        String maker = "41" + pid.toUpperCase(Locale.ROOT);
        int idx = hex.indexOf(maker);

        if(idx < 0) return new String[0];

        String payload = hex.substring(idx);

        List<String> bytes = new ArrayList<>();

        for(int i = 0; i+1 < payload.length(); i+=2){
            bytes.add(payload.substring(i, 1+2));
        }

        return bytes.toArray(new String[0]);
    }

}
