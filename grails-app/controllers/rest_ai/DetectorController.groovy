package rest_ai
import groovy.json.*
import groovy.json.JsonSlurper
import groovy.transform.Synchronized

class DetectorController {
    static Process process = null;
    static BufferedReader reader;
    static BufferedWriter writer;
    static OutputStream outputStream;
    static InputStream inputStream;

    @Synchronized
    def detectObjects() {
        String address = "";

        params.each {
            String val = it.value.toString()
            String key = it.key.toString()
            //possible candidate
            if(val.contains("png") || val.contains("jpg") || val.contains("jpeg")) {
                if (val.contains("http")) {
                    address = val
                }
            } else if(key.contains("png") || key.contains("jpg") || key.contains("jpeg")){
                if (key.contains("http")) {
                    address = key
                }
            }
        }

        if(address.contains("jpeg")) {
            address = address.substring(address.indexOf("http"), address.indexOf("jpeg")+4)
        } else if(address.contains("jpg")) {
            address = address.substring(address.indexOf("http"), address.indexOf("jpg")+3)
        } else {
            address = address.substring(address.indexOf("http"), address.indexOf("png")+3)
        }

        println address
        //something isn't right with received data
        if(address.size() == 0) {
            response.sendError(400)
            return
        }

        download(address)

        if(process == null) { // launch network
            String homeDirectory = System.getProperty("user.home");
            homeDirectory += "/Downloads/darknet/";

            if(System.getProperty("os.name").contains("Windows")) {
                process = Runtime.getRuntime().exec(String.format("cmd.exe  /c dir %s", homeDirectory));
            } else {
                process = Runtime.getRuntime().exec(String.format("./darknet detect cfg/yolov2-tiny.cfg cfg/yolov2-tiny.weights", homeDirectory));
            }

            outputStream = process.getOutputStream();
            inputStream = process.getInputStream();

            reader = new BufferedReader(new InputStreamReader(inputStream));
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        }

        writer.write("photos/${data.tokenize('/')[-1]}");
        writer.write(System.getProperty("line.separator"));
        writer.flush();

        Runtime.getRuntime().exec("clear");
        // need to gather results of network

        String line;
        String json = '['
        boolean state = false

        while ((line = reader.readLine()) != null) { // get line of my input given to program
            line = reader.readLine() // eat newline character
            line = reader.readLine() // get data
            if(line.contains("%")) {
                // have name of item as well as probability
                StringTokenizer defaultTokenizer = new StringTokenizer(line);

                while (defaultTokenizer.hasMoreTokens()) {
                    if (state) {
                        json += ","
                    }

                    String item = defaultTokenizer.nextToken();
                    item = item.substring(0, item.size() - 1); // trim off colon

                    json += '{ "' + item + '": "' + defaultTokenizer.nextToken() + '",\n'; // builds item and prob
                    // build cooordinates around it
                    json += '"left": "' + defaultTokenizer.nextToken() + '",\n"bot": "' + defaultTokenizer.nextToken() +
                            '",\n"right": "' + defaultTokenizer.nextToken() + '",\n"top": "' + defaultTokenizer.nextToken() + '"}'

                    state = true
                }
            }
            break;
        }
        json += ']'
        println json
        def results = new JsonSlurper().parseText(json)
        render results;
    }

    void download(def address) {
        new File("./photos/${address.tokenize('/')[-1]}").withOutputStream { out ->
            out << new URL(address).openStream()
        }
    }
}

