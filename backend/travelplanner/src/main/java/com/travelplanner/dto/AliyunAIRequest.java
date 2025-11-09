package com.travelplanner.dto;

import java.util.Map;

public class AliyunAIRequest {
    private String model;
    private Input input;
    private Parameters parameters;

    public static class Input {
        private String prompt;

        public Input() {}

        public Input(String prompt) {
            this.prompt = prompt;
        }

        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }

    public static class Parameters {
        private String result_format = "text";
        private Double top_p = 0.8;
        private Double temperature = 0.7;

        public String getResult_format() { return result_format; }
        public void setResult_format(String result_format) { this.result_format = result_format; }

        public Double getTop_p() { return top_p; }
        public void setTop_p(Double top_p) { this.top_p = top_p; }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }
    }

    // 构造方法
    public AliyunAIRequest() {}

    public AliyunAIRequest(String prompt) {
        this.model = "qwen-plus";
        this.input = new Input(prompt);
        this.parameters = new Parameters();
    }

    // getters and setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Input getInput() { return input; }
    public void setInput(Input input) { this.input = input; }

    public Parameters getParameters() { return parameters; }
    public void setParameters(Parameters parameters) { this.parameters = parameters; }
}