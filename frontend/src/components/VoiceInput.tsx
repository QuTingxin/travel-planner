import React from 'react';
import { Button, message, Space } from 'antd';
import { AudioOutlined, StopOutlined } from '@ant-design/icons';
import { useSpeechRecognition } from '../utils/speechRecognition';

interface VoiceInputProps {
  onResult: (text: string) => void;
  disabled?: boolean;
}

const VoiceInput: React.FC<VoiceInputProps> = ({ onResult, disabled = false }) => {
  const { isListening, transcript, error, startListening, stopListening } = useSpeechRecognition();

  const handleStartListening = async () => {
    try {
      const result = await startListening();
      onResult(result);
    } catch (err) {
      message.error('语音识别失败，请重试');
    }
  };

  React.useEffect(() => {
    if (error) {
      message.error(error);
    }
  }, [error]);

  return (
    <Space>
      <Button
        type="primary"
        icon={<AudioOutlined />}
        loading={isListening}
        onClick={handleStartListening}
        disabled={disabled || isListening}
      >
        {isListening ? '聆听中...' : '语音输入'}
      </Button>
      
      {isListening && (
        <Button
          danger
          icon={<StopOutlined />}
          onClick={stopListening}
        >
          停止
        </Button>
      )}
    </Space>
  );
};

export default VoiceInput;