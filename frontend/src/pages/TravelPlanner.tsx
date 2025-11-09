import React, { useState, useEffect } from 'react';
import { 
  Button, 
  Row, 
  Col, 
  Card, 
  Form, 
  Input, 
  DatePicker, 
  InputNumber, 
  Select, 
  message,
  Modal,
  Descriptions,
  Space,
  Divider,
} from 'antd';
import { PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { TravelPlan } from '../types';
import { travelPlanAPI } from '../services/api';
import TravelPlanCard from '../components/TravelPlanCard';
import VoiceInput from '../components/VoiceInput';

const { Option } = Select;
const { RangePicker } = DatePicker;
const { TextArea } = Input;

const TravelPlanner: React.FC = () => {
  const [plans, setPlans] = useState<TravelPlan[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalVisible, setModalVisible] = useState(false);
  const [detailModalVisible, setDetailModalVisible] = useState(false);
  const [selectedPlan, setSelectedPlan] = useState<TravelPlan | null>(null);
  const [searchText, setSearchText] = useState('');
  const [form] = Form.useForm();

  useEffect(() => {
    loadPlans();
  }, []);

  const loadPlans = async () => {
    setLoading(true);
    try {
      const data = await travelPlanAPI.getAll();
      setPlans(data);
    } catch (error) {
      message.error('加载旅行计划失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCreatePlan = async (values: any) => {
    try {
      const planData = {
        ...values,
        startDate: values.dates[0].format('YYYY-MM-DD'),
        endDate: values.dates[1].format('YYYY-MM-DD'),
      };
      delete planData.dates;

      await travelPlanAPI.create(planData);
      message.success('旅行计划创建成功');
      setModalVisible(false);
      form.resetFields();
      loadPlans();
    } catch (error) {
      message.error('创建旅行计划失败');
    }
  };

  const handleDeletePlan = async (id: number) => {
    try {
      await travelPlanAPI.delete(id);
      message.success('删除成功');
      loadPlans();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const handleSearch = async () => {
    setLoading(true);
    try {
      const data = await travelPlanAPI.search(searchText);
      setPlans(data);
    } catch (error) {
      message.error('搜索失败');
    } finally {
      setLoading(false);
    }
  };

  const handleVoiceResult = (text: string) => {
    form.setFieldsValue({
      preferences: text
    });
    message.info(`语音输入: ${text}`);
  };

  const showPlanDetail = (plan: TravelPlan) => {
    setSelectedPlan(plan);
    setDetailModalVisible(true);
  };

  return (
    <div style={{ padding: 24 }}>
      {/* 使用新的 PageHeader 组件 */}
      <div style={{ marginBottom: 24 }}>
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center',
          marginBottom: 16 
        }}>
          <div>
            <h1 style={{ 
              fontSize: 24, 
              fontWeight: 'bold', 
              margin: 0,
              color: '#1890ff'
            }}>
              智能旅行规划
            </h1>
            <p style={{ 
              margin: 0, 
              color: '#666',
              fontSize: 14
            }}>
              创建和管理您的旅行计划
            </p>
          </div>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={() => setModalVisible(true)}
          >
            新建旅行计划
          </Button>
        </div>
      </div>

      {/* 搜索栏 */}
      <Card style={{ marginBottom: 24 }}>
        <Row gutter={16} align="middle">
          <Col flex="auto">
            <Input
              placeholder="搜索目的地..."
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              onPressEnter={handleSearch}
            />
          </Col>
          <Col>
            <Button 
              type="primary" 
              icon={<SearchOutlined />}
              onClick={handleSearch}
            >
              搜索
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 旅行计划列表 */}
      <div>
        {plans.map(plan => (
          <TravelPlanCard
            key={plan.id}
            plan={plan}
            onEdit={() => {
              message.info('编辑功能开发中...');
            }}
            onDelete={handleDeletePlan}
            onView={showPlanDetail}
          />
        ))}
        
        {plans.length === 0 && !loading && (
          <Card style={{ textAlign: 'center', color: '#999' }}>
            暂无旅行计划，点击"新建旅行计划"开始规划
          </Card>
        )}
      </div>

      {/* 创建旅行计划模态框 */}
      <Modal
        title="新建旅行计划"
        open={modalVisible}
        onCancel={() => setModalVisible(false)}
        footer={null}
        width={700}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreatePlan}
        >
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="destination"
                label="目的地"
                rules={[{ required: true, message: '请输入目的地' }]}
              >
                <Input placeholder="例如：日本东京" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="dates"
                label="旅行日期"
                rules={[{ required: true, message: '请选择旅行日期' }]}
              >
                <RangePicker style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col span={12}>
              <Form.Item
                name="budget"
                label="总预算（元）"
                rules={[{ required: true, message: '请输入预算' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  formatter={value => `¥ ${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={value => value?.replace(/¥\s?|(,*)/g, '') as any}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item
                name="travelerCount"
                label="旅行人数"
                rules={[{ required: true, message: '请输入旅行人数' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  min={1}
                  max={20}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="preferences"
            label={
              <Space>
                <span>旅行偏好</span>
                <VoiceInput onResult={handleVoiceResult} />
              </Space>
            }
          >
            <TextArea
              rows={3}
              placeholder="例如：喜欢美食和动漫，带孩子，偏好文化体验..."
            />
          </Form.Item>

          <Form.Item style={{ textAlign: 'right', marginBottom: 0 }}>
            <Button onClick={() => setModalVisible(false)} style={{ marginRight: 8 }}>
              取消
            </Button>
            <Button type="primary" htmlType="submit">
              生成智能行程
            </Button>
          </Form.Item>
        </Form>
      </Modal>

      {/* 行程详情模态框 */}
      <Modal
        title="旅行计划详情"
        open={detailModalVisible}
        onCancel={() => setDetailModalVisible(false)}
        footer={[
          <Button key="close" onClick={() => setDetailModalVisible(false)}>
            关闭
          </Button>
        ]}
        width={800}
      >
        {selectedPlan && (
          <div>
            <Descriptions title="基本信息" bordered column={2}>
              <Descriptions.Item label="目的地">{selectedPlan.destination}</Descriptions.Item>
              <Descriptions.Item label="旅行日期">
                {selectedPlan.startDate} 至 {selectedPlan.endDate}
              </Descriptions.Item>
              <Descriptions.Item label="预算">¥{selectedPlan.budget.toLocaleString()}</Descriptions.Item>
              <Descriptions.Item label="旅行人数">{selectedPlan.travelerCount}人</Descriptions.Item>
              <Descriptions.Item label="旅行偏好" span={2}>
                {selectedPlan.preferences}
              </Descriptions.Item>
            </Descriptions>

            <Divider />

            <div>
              <h3>智能行程规划</h3>
              <div style={{ 
                background: '#f5f5f5', 
                padding: 16, 
                borderRadius: 4,
                whiteSpace: 'pre-wrap',
                lineHeight: 1.6
              }}>
                {selectedPlan.itinerary || '暂无行程详情'}
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
};

export default TravelPlanner;