import { describe, expect, it } from 'vitest'
import { renderArticleMarkdown } from './articleMarkdown'

describe('renderArticleMarkdown', () => {
  it('preserves two leading spaces for normal paragraphs', () => {
    expect(renderArticleMarkdown('  AI 时代')).toContain('<p>  AI 时代</p>')
  })

  it('does not turn markdown lists into indented paragraphs', () => {
    expect(renderArticleMarkdown('  - 列表项')).toContain('<ul>')
  })
})
