import React from 'react';
import { Layout, Menu, Button, Dropdown, Avatar, Space } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { 
    LogoutOutlined, 
    UserOutlined, 
    MenuFoldOutlined, 
    MenuUnfoldOutlined,
    CompassOutlined 
} from '@ant-design/icons';

const { Header, Sider, Content } = Layout;

const AppLayout: React.FC = () => {
    const [collapsed, setCollapsed] = React.useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    const handleLogout = () => {
        localStorage.removeItem('token');
        localStorage.removeItem('username');
        navigate('/login');
    };

    const userMenuItems = [
        {
            key: 'profile',
            icon: <UserOutlined />,
            label: '个人资料',
        },
        {
            type: 'divider' as const,
        },
        {
            key: 'logout',
            icon: <LogoutOutlined />,
            label: '退出登录',
            onClick: handleLogout,
        },
    ];

    const menuItems = [
        {
            key: '/',
            icon: <CompassOutlined />,
            label: '旅行规划',
        },
    ];

    return (
        <Layout style={{ minHeight: '100vh' }}>
            <Sider 
                trigger={null} 
                collapsible 
                collapsed={collapsed}
                style={{
                    background: '#fff',
                    boxShadow: '2px 0 6px rgba(0,21,41,0.1)'
                }}
            >
                <div style={{ 
                    height: 64, 
                    padding: '16px', 
                    display: 'flex', 
                    alignItems: 'center',
                    justifyContent: collapsed ? 'center' : 'flex-start',
                    borderBottom: '1px solid #f0f0f0'
                }}>
                    <CompassOutlined style={{ fontSize: 24, color: '#1890ff' }} />
                    {!collapsed && (
                        <span style={{ 
                            marginLeft: 8, 
                            fontSize: 16, 
                            fontWeight: 'bold',
                            color: '#1890ff'
                        }}>
                            智能旅行
                        </span>
                    )}
                </div>
                
                <Menu
                    mode="inline"
                    selectedKeys={[location.pathname]}
                    items={menuItems}
                    onClick={({ key }) => navigate(key)}
                    style={{ borderRight: 0, marginTop: 8 }}
                />
            </Sider>
            
            <Layout>
                <Header style={{ 
                    padding: '0 16px', 
                    background: '#fff',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    boxShadow: '0 1px 4px rgba(0,21,41,0.08)'
                }}>
                    <Button
                        type="text"
                        icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                        onClick={() => setCollapsed(!collapsed)}
                        style={{ fontSize: '16px', width: 64, height: 64 }}
                    />
                    
                    <Space>
                        <span style={{ color: '#666' }}>
                            欢迎，{localStorage.getItem('username')}
                        </span>
                        <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                            <Avatar 
                                style={{ backgroundColor: '#1890ff', cursor: 'pointer' }}
                                icon={<UserOutlined />}
                            />
                        </Dropdown>
                    </Space>
                </Header>
                
                <Content style={{ 
                    margin: '24px 16px', 
                    padding: 24, 
                    background: '#fff',
                    borderRadius: 6,
                    minHeight: 280,
                    overflow: 'auto'
                }}>
                    <Outlet />
                </Content>
            </Layout>
        </Layout>
    );
};

export default AppLayout;