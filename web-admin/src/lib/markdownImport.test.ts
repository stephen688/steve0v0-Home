import { describe, expect, it } from 'vitest'
import { parseImportedArticle } from './markdownImport'

describe('parseImportedArticle', () => {
  it('extracts a plain first-line title and leading quote summary', () => {
    const result = parseImportedArticle('从零到一，我的第一个全栈项目\n\n> 一个大一新生的真实记录：\n> 从小白到完成全栈项目。\n\n---\n\n正文')

    expect(result.title).toBe('从零到一，我的第一个全栈项目')
    expect(result.summary).toBe('一个大一新生的真实记录： 从小白到完成全栈项目。')
    expect(result.content).toBe('---\n\n正文')
  })

  it('preserves content when the first block is not a title', () => {
    const result = parseImportedArticle('- 第一项\n- 第二项')

    expect(result.title).toBe('')
    expect(result.summary).toBe('')
    expect(result.content).toBe('- 第一项\n- 第二项')
  })
})
