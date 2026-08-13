import { SITE_CONFIG } from '../../config/site';
import { api } from '../../services/api';
import { AboutProfile, AboutProject } from '../../types/api';
import { copyText } from '../../utils/clipboard';

interface TimelineItem {
  date: string;
  title: string;
  description: string;
}

interface AnimatedSkill {
  name: string;
  value: number;
  detail: string;
  displayValue: number;
}

let skillAnimationDelay: number | undefined;
let skillAnimationTimer: number | undefined;

function clearSkillAnimation() {
  if (skillAnimationDelay !== undefined) clearTimeout(skillAnimationDelay);
  if (skillAnimationTimer !== undefined) clearInterval(skillAnimationTimer);
  skillAnimationDelay = undefined;
  skillAnimationTimer = undefined;
}

Page({
  data: {
    profile: null as AboutProfile | null,
    projects: [] as AboutProject[],
    profileLoading: true,
    projectLoading: true,
    profileError: false,
    projectError: false,
    site: SITE_CONFIG,
    timeline: SITE_CONFIG.timeline as TimelineItem[],
    contacts: SITE_CONFIG.contacts,
    tags: SITE_CONFIG.tags,
    skills: SITE_CONFIG.skills.map((skill) => ({ ...skill, displayValue: 0 })) as AnimatedSkill[]
  },

  onLoad() {
    this.loadProfile();
    this.loadProjects();
  },

  onShow() {
    this.playSkillAnimation();
  },

  onHide() {
    clearSkillAnimation();
  },

  onUnload() {
    clearSkillAnimation();
  },

  playSkillAnimation() {
    clearSkillAnimation();
    const resetSkills = SITE_CONFIG.skills.map((skill) => ({ ...skill, displayValue: 0 })) as AnimatedSkill[];
    this.setData({ skills: resetSkills });

    skillAnimationDelay = setTimeout(() => {
      const startedAt = Date.now();
      const duration = 900;
      skillAnimationTimer = setInterval(() => {
        const progress = Math.min((Date.now() - startedAt) / duration, 1);
        const easedProgress = 1 - Math.pow(1 - progress, 3);
        const skills = SITE_CONFIG.skills.map((skill) => ({
          ...skill,
          displayValue: Math.round(skill.value * easedProgress)
        })) as AnimatedSkill[];
        this.setData({ skills });
        if (progress === 1) clearSkillAnimation();
      }, 16);
    }, 80);
  },

  onPullDownRefresh() {
    this.loadProfile();
    this.loadProjects();
    setTimeout(() => wx.stopPullDownRefresh(), 1000);
  },

  loadProfile() {
    this.setData({ profileLoading: true, profileError: false });
    api.getProfile().then((profile) => this.setData({ profile, profileLoading: false })).catch(() => this.setData({ profileLoading: false, profileError: true }));
  },

  loadProjects() {
    this.setData({ projectLoading: true, projectError: false });
    api.getProjects().then((projects) => this.setData({ projects: projects || [], projectLoading: false })).catch(() => this.setData({ projectLoading: false, projectError: true }));
  },

  retryProfile() { this.loadProfile(); },
  retryProjects() { this.loadProjects(); },

  copyContact(event: WechatPageEvent) {
    const index = Number(event.currentTarget?.dataset?.index || 0);
    const contact = this.data.contacts[index];
    if (contact?.value) copyText(contact.value, '联系方式已复制');
  },

  noop() {}
});
