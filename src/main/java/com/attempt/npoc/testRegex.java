package com.attempt.npoc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testRegex {
 
    public static void main(String[] args) {
        Pattern re = Pattern.compile("\"code_uid\":\"(\\{[\\w-]+\\})\"");
        Matcher matcher = re.matcher("�{\"status\":1,\"code_uid\":\"{65F40AC5-F5E3-0478-71D9-1E3D1C0FC094}\"}");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }
}
