import React, { useState } from 'react';
import { 
    Card, 
    Button, 
    Space, 
    message, 
    Typography, 
    Spin,
    Result,
    Divider,
    Descriptions
} from 'antd';
import { 
    AudioOutlined, 
    StopOutlined, 
    CheckCircleOutlined,
    ReloadOutlined 
} from '@ant-design/icons';
import { useSpeechRecognition } from '../utils/speechRecognition';
import { travelPlanAPI, voicePlanAPI } from '../services/api';
import { TravelPlan } from '../types';

const { Title, Paragraph } = Typography;

const VoicePlanner: React.FC = () => {
    const [isGenerating, setIsGenerating] = useState(false);
    const [generatedPlan, setGeneratedPlan] = useState<TravelPlan | null>(null);
    const [aiAnalysis, setAiAnalysis] = useState<any>(null);
    
    const { 
        isListening, 
        transcript, 
        error, 
        startListening, 
        stopListening 
    } = useSpeechRecognition();

    const handleStartListening = async () => {
        try {
            setGeneratedPlan(null);
            setAiAnalysis(null);
            await startListening();
        } catch (err) {
            message.error('语音识别失败，请重试');
        }
    };

    // const handleGeneratePlan = async () => {
    //     if (!transcript.trim()) {
    //         message.warning('请先进行语音输入');
    //         return;
    //     }

    //     setIsGenerating(true);
    //     try {
    //         const response = await fetch('http://localhost:8080/api/voice-plan/generate', {
    //             method: 'POST',
    //             headers: {
    //                 'Content-Type': 'application/json',
    //                 'Authorization': `Bearer ${localStorage.getItem('token')}`
    //             },
    //             body: JSON.stringify({
    //                 voiceText: transcript
    //             })
    //         });

    //         if (!response.ok) {
    //             throw new Error('生成计划失败');
    //         }

    //         const data = await response.json();
    //         setGeneratedPlan(data.plan);
    //         setAiAnalysis(data.aiAnalysis);
    //         message.success('旅行计划生成成功！');

    //     } catch (error) {
    //         console.error('生成计划失败:', error);
    //         message.error('生成旅行计划失败，请重试');
    //     } finally {
    //         setIsGenerating(false);
    //     }
    // };


    // 在 handleGeneratePlan 方法中，替换 fetch 调用为：
const handleGeneratePlan = async () => {
    if (!transcript.trim()) {
        message.warning('请先进行语音输入');
        return;
    }

    setIsGenerating(true);
    try {
        // 使用统一的 API 服务而不是直接 fetch
        const response = await voicePlanAPI.generate(transcript);
        
        setGeneratedPlan(response.plan);
        setAiAnalysis(response.aiAnalysis);
        message.success('旅行计划生成成功！');

    } catch (error) {
        console.error('生成计划失败:', error);
        message.error('生成旅行计划失败，请重试');
    } finally {
        setIsGenerating(false);
    }
};

    const handleCreateNew = () => {
        setGeneratedPlan(null);
        setAiAnalysis(null);
        stopListening();
    };

    return (
        <div style={{ padding: 24, maxWidth: 800, margin: '0 auto' }}>
            <Title level={2} style={{ textAlign: 'center', marginBottom: 8 }}>
                🎤 语音智能旅行规划
            </Title>
            <Paragraph style={{ textAlign: 'center', color: '#666', marginBottom: 32 }}>
                请用语音描述您的旅行需求，AI将为您生成完整的旅行计划
            </Paragraph>

            {!generatedPlan ? (
                <Card>
                    {/* 语音输入区域 */}
                    <div style={{ textAlign: 'center', marginBottom: 24 }}>
                        <div style={{ 
                            background: '#f5f5f5', 
                            padding: 24, 
                            borderRadius: 8,
                            marginBottom: 16
                        }}>
                            {transcript ? (
                                <Paragraph style={{ 
                                    fontSize: 16, 
                                    lineHeight: 1.6,
                                    minHeight: 60
                                }}>
                                    {transcript}
                                </Paragraph>
                            ) : (
                                <Paragraph style={{ 
                                    color: '#999', 
                                    minHeight: 60,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center'
                                }}>
                                    {isListening ? '正在聆听...' : '请点击下方按钮开始语音输入'}
                                </Paragraph>
                            )}
                        </div>

                        <Space size="large">
                            <Button
                                type="primary"
                                size="large"
                                icon={<AudioOutlined />}
                                loading={isListening}
                                onClick={handleStartListening}
                                disabled={isListening}
                                style={{ 
                                    width: 120,
                                    height: 120,
                                    borderRadius: '50%',
                                    fontSize: 16
                                }}
                            >
                                {isListening ? '聆听中' : '开始说话'}
                            </Button>
                            
                            {isListening && (
                                <Button
                                    danger
                                    size="large"
                                    icon={<StopOutlined />}
                                    onClick={stopListening}
                                    style={{ 
                                        width: 120,
                                        height: 120,
                                        borderRadius: '50%',
                                        fontSize: 16
                                    }}
                                >
                                    停止
                                </Button>
                            )}
                        </Space>
                    </div>

                    {/* 操作按钮 */}
                    <div style={{ textAlign: 'center' }}>
                        <Button
                            type="primary"
                            size="large"
                            loading={isGenerating}
                            onClick={handleGeneratePlan}
                            disabled={!transcript.trim() || isListening}
                            style={{ minWidth: 200 }}
                        >
                            {isGenerating ? 'AI规划中...' : '生成旅行计划'}
                        </Button>
                    </div>

                    {/* 使用提示 */}
                    <Divider>使用提示</Divider>
                    <div style={{ color: '#666', fontSize: 14 }}>
                        <Paragraph>
                            <strong>语音输入示例：</strong>
                        </Paragraph>
                        <ul>
                            <li>"我想去日本东京玩5天，预算1万元，两个人，喜欢美食和动漫"</li>
                            <li>"下个月带家人去三亚度假，4天3晚，预算8000元，要住海景房"</li>
                            <li>"国庆节和朋友去成都，3天时间，人均2000元，主要想吃火锅看熊猫"</li>
                        </ul>
                    </div>
                </Card>
            ) : (
                <Card>
                    <Result
                        icon={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
                        title="旅行计划生成成功！"
                        extra={[
                            <Button 
                                key="new" 
                                icon={<ReloadOutlined />} 
                                onClick={handleCreateNew}
                            >
                                创建新计划
                            </Button>,
                            <Button 
                                key="view" 
                                type="primary"
                                onClick={() => window.location.href = '/'}
                            >
                                查看所有计划
                            </Button>
                        ]}
                    />

                    <Divider>计划详情</Divider>
                    
                    {/* 基本信息 */}
                    <Descriptions title="旅行基本信息" bordered column={2} style={{ marginBottom: 24 }}>
                        <Descriptions.Item label="目的地">{generatedPlan.destination}</Descriptions.Item>
                        <Descriptions.Item label="旅行日期">
                            {generatedPlan.startDate} 至 {generatedPlan.endDate}
                        </Descriptions.Item>
                        <Descriptions.Item label="预算">¥{generatedPlan.budget?.toLocaleString()}</Descriptions.Item>
                        <Descriptions.Item label="旅行人数">{generatedPlan.travelerCount}人</Descriptions.Item>
                        <Descriptions.Item label="旅行偏好" span={2}>
                            {generatedPlan.preferences}
                        </Descriptions.Item>
                    </Descriptions>

                    {/* AI生成的行程 */}
                    <div>
                        <Title level={4}>📅 智能行程规划</Title>
                        <div style={{ 
                            background: '#f9f9f9', 
                            padding: 16, 
                            borderRadius: 6,
                            whiteSpace: 'pre-wrap',
                            lineHeight: 1.6,
                            fontSize: 14
                        }}>
                            {generatedPlan.itinerary}
                        </div>
                    </div>
                </Card>
            )}

            {error && (
                <Card style={{ marginTop: 16, background: '#fff2f0' }}>
                    <Paragraph type="danger">
                        语音识别错误: {error}
                    </Paragraph>
                </Card>
            )}
        </div>
    );
};

export default VoicePlanner;