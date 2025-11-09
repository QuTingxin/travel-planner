// 语音识别工具函数
export class SpeechRecognitionUtil {
  private recognition: any;
  private isListening: boolean = false;

  constructor() {
    // 检查浏览器支持
    const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;
    
    if (!SpeechRecognition) {
      throw new Error('您的浏览器不支持语音识别功能');
    }

    this.recognition = new SpeechRecognition();
    this.recognition.continuous = false;
    this.recognition.interimResults = false;
    this.recognition.lang = 'zh-CN';
  }

  // 开始语音识别
  start(): Promise<string> {
    return new Promise((resolve, reject) => {
      if (this.isListening) {
        reject(new Error('语音识别正在进行中'));
        return;
      }

      this.isListening = true;

      this.recognition.onresult = (event: any) => {
        const transcript = event.results[0][0].transcript;
        this.isListening = false;
        resolve(transcript);
      };

      this.recognition.onerror = (event: any) => {
        this.isListening = false;
        reject(new Error(`语音识别错误: ${event.error}`));
      };

      this.recognition.onend = () => {
        this.isListening = false;
      };

      this.recognition.start();
    });
  }

  // 停止语音识别
  stop(): void {
    if (this.isListening) {
      this.recognition.stop();
      this.isListening = false;
    }
  }

  // 检查是否支持语音识别
  static isSupported(): boolean {
    return !!(window.SpeechRecognition || (window as any).webkitSpeechRecognition);
  }
}

// 语音识别Hook
import { useState, useCallback } from 'react';

export const useSpeechRecognition = () => {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [error, setError] = useState<string | null>(null);

  const startListening = useCallback(async () => {
    try {
      if (!SpeechRecognitionUtil.isSupported()) {
        throw new Error('浏览器不支持语音识别');
      }

      setIsListening(true);
      setError(null);
      
      const recognition = new SpeechRecognitionUtil();
      const result = await recognition.start();
      
      setTranscript(result);
      setIsListening(false);
      return result;
    } catch (err) {
      setError(err instanceof Error ? err.message : '语音识别失败');
      setIsListening(false);
      throw err;
    }
  }, []);

  const stopListening = useCallback(() => {
    setIsListening(false);
  }, []);

  return {
    isListening,
    transcript,
    error,
    startListening,
    stopListening,
  };
};