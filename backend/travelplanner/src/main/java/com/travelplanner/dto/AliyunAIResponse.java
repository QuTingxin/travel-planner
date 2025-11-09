package com.travelplanner.dto;

import java.util.Map;

public class AliyunAIResponse {
    private Output output;
    private Usage usage;
    private String request_id;

    public static class Output {
        private String text;
        private String finish_reason;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public String getFinish_reason() { return finish_reason; }
        public void setFinish_reason(String finish_reason) { this.finish_reason = finish_reason; }
    }

    public static class Usage {
        private InputOutputTokens input_tokens;
        private InputOutputTokens output_tokens;

        public static class InputOutputTokens {
            private Integer tokens;

            public Integer getTokens() { return tokens; }
            public void setTokens(Integer tokens) { this.tokens = tokens; }
        }

        public InputOutputTokens getInput_tokens() { return input_tokens; }
        public void setInput_tokens(InputOutputTokens input_tokens) { this.input_tokens = input_tokens; }

        public InputOutputTokens getOutput_tokens() { return output_tokens; }
        public void setOutput_tokens(InputOutputTokens output_tokens) { this.output_tokens = output_tokens; }
    }

    // getters and setters
    public Output getOutput() { return output; }
    public void setOutput(Output output) { this.output = output; }

    public Usage getUsage() { return usage; }
    public void setUsage(Usage usage) { this.usage = usage; }

    public String getRequest_id() { return request_id; }
    public void setRequest_id(String request_id) { this.request_id = request_id; }
}