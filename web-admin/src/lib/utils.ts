import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

/**
 * 合并 Tailwind 类名工具
 * 支持条件类、数组、对象，并自动解决类名冲突
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
