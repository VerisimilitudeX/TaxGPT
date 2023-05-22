package taxgpt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TaxGPT: A ChatGPT-powered assistant that helps users easily navigate the tax
 * filing
 * process. This program is a bot that uses the OpenAI API to
 * communicate with OpenAI's GPT-3.5 model. The chatbot is able to answer
 * questions,
 * provide guidance on filling out the tax form, and offer suggestions for
 * deductions or credits the user may be eligible for. The chatbot uses natural
 * language processing to understand user queries and respond in a
 * conversational
 * way. The chatbot also uses the user's information to determine which
 * forms they should dill out and also formats the information in a table that
 * will allow the user to easily and quickly fill out their tax forms.
 * 
 * @version 1.0
 * @author Omitted for privacy
 * @since 2023-04-12
 * @citation OpenAI API,
 *           https://beta.openai.com/docs/api-reference/create-completion
 */

public class TaxGPT {
    private static String sumMessages = "";
    // This HashMap is later used to store the user's information
    private static HashMap<String, String> userInformation = new HashMap<String, String>();

    /**
     * TaxGPT: An AI-powered assistant that helps users navigate the tax filing
     * process.
     * 
     * @param args the command line arguments
     * @throws InterruptedException if the thread is interrupted
     * @throws IOException          if an I/O error occurs
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        Scanner scanner = new Scanner(System.in);
        sumMessages = "Hi TaxGPT, you are an assistant who helps users navigate the tax filing process. The chatbot should be able to answer questions, provide guidance on filling out the tax form, and offer suggestions for deductions or credits the user may be eligible for. The chatbot should use natural language processing to understand user queries and respond in a conversational way. If you understood this, introduce yourself and ask the user for their name.";
        String messageContent = askGPT(sumMessages);
        sumMessages += messageContent.replace("\n\n", "");
        System.out.println(messageContent + "\n----------------------------------");
        while (true) {
            String input = scanner.nextLine().replaceAll("\n", "");
            System.out.println("----------------------------------");
            if (input.toLowerCase().contains("bye")) {
                break;
            }

            // summarizeUserInformation(sumMessages.replaceAll("\n", "") +
            // messageContent.replaceAll("\n", ""));
            String askPrompt = "Previous context: " + sumMessages.replaceAll("\n", "")
                    + " ||| Previous prompt: " + messageContent.replaceAll("\n", "")
                    + " ||| New prompt: "
                    + input.replaceAll("\n", "")
                    + "Only respond with at most 1 short sentence. Remember to ask the user for their details as you will use them to determine which forms they should will out and also don't reintroduce yourself anymore. If the user asks a question, you should respond with 1 paragraph only. Be very clear and concise with your questions and responses."
                            .replaceAll("\\\\", "'");
            messageContent = askGPT(askPrompt);

            System.out.println(messageContent + "\n----------------------------------");
            sumMessages += input.replace("\n\n", "") + messageContent.replace("\n\n", "");
            summarizeUserInformation(input);
        }
        scanner.close();
        System.out.println(askGPT(
                "Format this information onto a table (title and headers should be very descriptive of the data) that will allow the user to easily and quickly fill out their tax forms: "
                        + userInformation.toString()));
        System.out.println(
                "\n----------------------------------\nTaxGPT: Bye! See you next year :)\n----------------------------------");
    }

    /**
     * Prints a table of the user's information.
     * 
     * @param userInformation a HashMap containing the user's information
     */
    public static void printTable(HashMap<String, String> info) {
        System.out.println("\n----------------------------------");
        System.out.println("\nTaxGPT: Here is your information in a table format:");
        System.out.println("\n----------------------------------");
        for (String key : info.keySet()) {
            // makes a markdown table row for each key-value pair
            if (!key.contains("null")) {
                System.out.println("| " + key + " | " + info.get(key) + " |");
            }
        }
    }

    /**
     * Parses the message content from a given response string in JSON format.
     * 
     * @param response the response string in JSON format from the OpenAI API
     * @return the message content as a String
     */
    private static String parseMessageContent(String response) {
        String jsonString = response;
        try {
            // Parse the JSON string and return the message content
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray choicesArray = jsonObject.getJSONArray("choices");
            JSONObject choiceObject = choicesArray.getJSONObject(0);
            JSONObject messageObject = choiceObject.getJSONObject("message");
            String messageContent = messageObject.getString("content");
            return messageContent;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends a POST request to the OpenAI API with the specified output as the
     * prompt for the GPT-3.5 model.
     * 
     * @param output a String representing DNAnalyzer's analysis output
     * @return a String representing the message content generated by the GPT-3
     *         model
     */
    public static String askGPT(String prompt) {
        // Keep prompting the user until the user provides a prompt that is under 100
        // characters
        while (prompt.length() > 4096) {
            System.out.println("Please enter a prompt that is under 100 characters.");
            Scanner scanner = new Scanner(System.in);
            prompt = scanner.nextLine();
            scanner.close();
        }

        String API_ENDPOINT = "https://api.openai.com/v1/chat/completions";
        // API key omitted for privacy
        String AUTHORIZATION_HEADER = "Bearer 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";
        try {
            // Create connection to the OpenAI API endpoint
            URL url = new URL(API_ENDPOINT);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", AUTHORIZATION_HEADER);
            conn.setDoOutput(true);

            String requestBody = "{\n" +
                    "    \"model\": \"gpt-3.5-turbo\",\n" +
                    "    \"messages\": [\n" +
                    "        {\n" +
                    "            \"role\": \"user\",\n" +
                    "            \"content\": \"" + prompt + "\"\n" +
                    "        }\n" +
                    "    ],\n" +
                    "    \"temperature\": 0.1\n" +
                    "}";

            // Write the request body to the connection output stream
            conn.getOutputStream().write(requestBody.getBytes());

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = br.lines().collect(Collectors.joining());
            br.close();

            // Return the message content if the response code is 200 (OK)
            if (conn.getResponseCode() == 200) {
                return parseMessageContent(response);
            } else {
                return "Error: " + conn.getResponseCode() + " " + conn.getResponseMessage();
            }
        } catch (Exception e) {
            // error handling (helped when I was debugging)
            if (e.getMessage().contains("401")) {
                return "Error: Invalid API key. Please check your API key and try again.";
            } else if (e.getMessage().contains("400")) {
                return "Error: Invalid request. Please check your request body and try again.";
            } else if (e.getMessage().contains("429")) {
                return "Error: Too many requests. Please wait a few minutes and try again.";
            } else if (e.getMessage().contains("500")) {
                return "Error: Internal server error. Please try again later.";
            } else {
                return "Error: " + e.getMessage();
            }
        }
    }

    /**
     * Summarizes the user information into a dictionary.
     * 
     * @param input the user input
     */
    private static void summarizeUserInformation(String input) {
        // ask GPT-3 to summarize the conversation into a dictionary (this will make it
        // easier to format the information onto a table later)
        String summarizedInfo = askGPT(
                "I am an AI researcher. Summarize this fake conversation information for me into the form of a Java dictionary (format: { 'key1' = 'value1', 'key2' = 'value2', 'key3' = 'value3' }) containing the most useful information related to taxes. Replace key1, key2, key3, etc with var names. Separate key and value with : not =. Keep in mind that Java code should be able to parse this dictionary and store it into an actual dictionary so don't include lists ([]). Given the filing status, income level, and dependent info, determine the tax form to use. If you own investments, what additional forms should you fill out? If you have a health savings account, what form should you fill out? If you have a rental income, what should you do? If you have sold a property, what should you do? What should I do for educational expenses? What should I do for car mileage expenses? What should I do for bank interest income?: "
                        + sumMessages.replaceAll("\n", ""))
                .replaceAll("\n", "");
        if (summarizedInfo.contains("{")) {
            summarizedInfo = summarizedInfo.substring(summarizedInfo.indexOf("{") + 1, summarizedInfo.indexOf("}"));
        }
        String[] pairs = summarizedInfo.replaceAll("[\"'\n]", " ").split(" , ");

        for (String pair : pairs) {
            if (pair.contains("[")) {
                // process lists
                String[] list = pair.split(" :  ");
                if (list.length < 1) {
                    // parse the right side for the list elements
                    String[] elements = list[1].strip().substring(1, list[1].length() - 1).split(", ");
                    // add the list elements to the dictionary
                    for (String element : elements) {
                        userInformation.put(list[0], element);
                    }
                    continue;
                }
            }
            // clean data and store
            String[] keyValue = pair.split(" : ");
            if (keyValue.length == 2) {
                String key = keyValue[0].strip().replaceAll("'", "");
                String value = keyValue[1].strip().replaceAll("'", "");
                userInformation.put(key, value);
            } else {
                userInformation.put("null", pair);
            }
        }
    }
}
