import React from 'react';
import { Card, Tag, Button, Space, Statistic, Popconfirm } from 'antd';
import { EditOutlined, DeleteOutlined, CalendarOutlined, UserOutlined, DollarOutlined } from '@ant-design/icons';
import { TravelPlan } from '../types';

interface TravelPlanCardProps {
  plan: TravelPlan;
  onEdit: (plan: TravelPlan) => void;
  onDelete: (id: number) => void;
  onView: (plan: TravelPlan) => void;
}

const TravelPlanCard: React.FC<TravelPlanCardProps> = ({ plan, onEdit, onDelete, onView }) => {
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('zh-CN');
  };

  const getDuration = (start: string, end: string) => {
    const startDate = new Date(start);
    const endDate = new Date(end);
    const duration = (endDate.getTime() - startDate.getTime()) / (1000 * 3600 * 24) + 1;
    return `${duration}天`;
  };

  return (
    <Card
      style={{ marginBottom: 16 }}
      actions={[
        <Button type="link" onClick={() => onView(plan)}>查看详情</Button>,
        <Button type="link" icon={<EditOutlined />} onClick={() => onEdit(plan)}>编辑</Button>,
        <Popconfirm
          title="确定删除这个旅行计划吗？"
          onConfirm={() => plan.id && onDelete(plan.id)}
          okText="确定"
          cancelText="取消"
        >
          <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
        </Popconfirm>,
      ]}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div style={{ flex: 1 }}>
          <h3 style={{ marginBottom: 8, color: '#1890ff' }}>{plan.destination}</h3>
          
          <Space size={[0, 8]} wrap style={{ marginBottom: 12 }}>
            <Tag icon={<CalendarOutlined />} color="blue">
              {formatDate(plan.startDate)} - {formatDate(plan.endDate)}
            </Tag>
            <Tag icon={<UserOutlined />} color="green">
              {plan.travelerCount}人
            </Tag>
            <Tag icon={<DollarOutlined />} color="orange">
              ¥{plan.budget.toLocaleString()}
            </Tag>
          </Space>
          
          {plan.preferences && (
            <div style={{ marginBottom: 8 }}>
              <strong>偏好：</strong>
              <span>{plan.preferences}</span>
            </div>
          )}
          
          {plan.itinerary && (
            <div style={{ 
              maxHeight: 100, 
              overflow: 'hidden', 
              background: '#f5f5f5', 
              padding: 8, 
              borderRadius: 4,
              fontSize: 12,
              color: '#666'
            }}>
              {plan.itinerary.substring(0, 150)}...
            </div>
          )}
        </div>
        
        <div style={{ marginLeft: 16, textAlign: 'right' }}>
          <Statistic
            title="行程天数"
            value={getDuration(plan.startDate, plan.endDate)}
            valueStyle={{ fontSize: 14 }}
          />
        </div>
      </div>
    </Card>
  );
};

export default TravelPlanCard;