import { ElNotification } from 'element-plus'

// 统一配置全局提示参数
const DURATION = 1500; // 强制所有提示 1.5 秒后消失
const POSITION = 'bottom-right'; // 强制在右下角弹出

// 图标与标题映射
const titleMap: Record<string, string> = {
  success: 'Success',
  warning: 'Warning',
  error: 'Error',
  info: 'Info'
}

// 核心转换逻辑：将 Message 参数转为 Notification 参数
const triggerNotification = (options: any, defaultType = 'info') => {
  let msg = '';
  let type = defaultType;

  // 兼容直接传字符串的情况，如 ElMessage.success('操作成功')
  if (typeof options === 'string') {
    msg = options;
  } else {
    // 兼容传对象的情况，如 ElMessage({ message: '...', type: '...' })
    msg = options.message || '';
    type = options.type || defaultType;
  }

  ElNotification({
    title: titleMap[type] || 'Notification',
    message: msg,
    type: type as any,
    position: POSITION,
    duration: DURATION,
    showClose: false // 1.5秒很快，不需要额外的关闭按钮
  });
}

// 1. 让它作为函数可被直接调用 (拦截 ElMessage({ ... }))
const CustomMessage: any = (options: any) => {
  triggerNotification(options);
}

// 2. 挂载快捷方法 (拦截 ElMessage.success('...') 等)
CustomMessage.success = (options: any) => triggerNotification(options, 'success')
CustomMessage.warning = (options: any) => triggerNotification(options, 'warning')
CustomMessage.error = (options: any) => triggerNotification(options, 'error')
CustomMessage.info = (options: any) => triggerNotification(options, 'info')

// 伪装成原生 ElMessage 暴露出去
export { CustomMessage as ElMessage }
