export interface SkillConfig {
  name: string;
  value: number;
  detail: string;
}

export interface ContactConfig {
  label: string;
  value: string;
  copyable?: boolean;
}

export const SITE_CONFIG = {
  tagline: '记录学习，也记录生活。',
  heroImage: '/assets/moments/moments-hero.jpg',
  name: 'steve0v0',
  motto: '“我们都有光明的未来”',
  profession: '全栈开发 · AI Agent 探索者',
  location: '广州',
  email: '6715425@qq.com',
  status: '持续探索中',
  tags: ['Spring Boot', '全栈开发', 'Vue 3', 'AI Agent'],
  skills: [
    { name: 'Frontend', value: 28, detail: 'Vue 3 / TypeScript' },
    { name: 'Backend', value: 70, detail: 'Java / Spring Boot' },
    { name: 'AI Agent', value: 46, detail: 'Prompt / Tool / RAG' },
    { name: 'DevOps', value: 50, detail: 'Docker / Linux / Git' },
    { name: 'Design', value: 24, detail: 'Figma / UI / UX' }
  ] as SkillConfig[],
  timeline: [
    {
      date: '2025.07',
      title: '入坑编程',
      description: '用 C 第一次跑通 Hello World，开始将代码融入生活。'
    },
    {
      date: '2025.10',
      title: 'Java SE + Spring Boot 学习',
      description: '入坑 Java，逐步了解 Java 基础、Spring 生态与开发相关流程。'
    },
    {
      date: '2026.04',
      title: '全栈项目实战',
      description: '从 0 到 1 搭建 QuantaCommunity 前后端，不断学习新框架，熟悉全栈开发流程。'
    },
    {
      date: '2026.07',
      title: 'AI Agent 探索',
      description: '开始研究 Prompt 设计、Tool 调用、RAG 链路、多 Agent 协作、Skill 优化与 Context 管理等知识。'
    }
  ],
  contacts: [
    { label: 'Email', value: '6715425@qq.com', copyable: true },
    { label: 'GitHub', value: 'https://github.com/stephen688', copyable: true },
    { label: '公众号', value: 'CodeInLife', copyable: true },
    { label: 'Bilibili', value: 'steve0v0', copyable: true },
    { label: 'Rednote', value: '余念已安', copyable: true },
    { label: 'Motto', value: '“我们都有光明的未来”', copyable: true }
  ] as ContactConfig[]
};
