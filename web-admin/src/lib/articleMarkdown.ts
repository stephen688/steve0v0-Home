import MarkdownIt from 'markdown-it'

const markdown = new MarkdownIt({ html: false, breaks: true, linkify: true })

/**
 * 将普通段落行首两个半角空格转为可见的全宽空格。
 * Markdown 会忽略 1〜3 个行首空格，但不应影响列表、引用、标题和代码块。
 */
function preserveParagraphIndent(source: string) {
  return source.replace(/^ {2}(?=[\u3400-\u9fffA-Za-z])/gm, '&emsp;&emsp;')
}

export function renderArticleMarkdown(source: string | null | undefined) {
  return markdown.render(preserveParagraphIndent(source || ''))
}

