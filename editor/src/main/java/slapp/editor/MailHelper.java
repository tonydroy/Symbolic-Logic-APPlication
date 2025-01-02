/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.  If not, see
<https://www.gnu.org/licenses/>.
 */

package slapp.editor;

/**
 * Send email by native email client
 */
public class MailHelper {

    /**
     * Open the email client
     * @param receiver message address to
     * @param subject message subject
     * @param body message body
     */
    public static void generate(String receiver, String subject, String body) {
        try {
            //Open mail client with "receiver", "subject", "message"
            composeEmail(receiver, subject, body);
        }
        catch (Exception err) {EditorAlerts.showSimpleAlert("Cannot Open", "Could not open default email application.  You may still send a message to messaging@slappservices.net with 'SLAPP' in subject line.");


        }
    }


    private static void composeEmail(String receiver, String subject, String body) throws Exception {
        //Generating mailto-URI. Subject and body (message) has to encoded.
        String mailto = "mailto:" + receiver;
        mailto += "?subject=" + uriEncode(subject);
        mailto += "&body=" + uriEncode(body);

        //Create OS-specific run command
        String cmd = "";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")){
            cmd = "cmd.exe /c start \"\" \"" + mailto + "\"";
        }
        else if (os.contains("mac")){
            cmd = "open " + mailto;
        }
        else if (os.contains("nix") || os.contains("aix") || os.contains("nux")){
            cmd = "xdg-open " + mailto;
        }
        //Call default mail client with paramters
        Runtime.getRuntime().exec(cmd);

    }

    /**
     * replace non-alphanumeric characters with escape code
     * @param in the input string
     * @return the string with replaced characters
     */
    private static String uriEncode(String in) {
        String out = new String();
        for (char ch : in.toCharArray()) {
            out += Character.isLetterOrDigit(ch) ? ch : String.format("%%%02X", (int)ch);
        }
        return out;
    }


}
