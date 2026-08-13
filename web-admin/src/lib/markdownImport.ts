export interface ImportedArticleContent {
  title: string
  summary: string
  content: string
}

export function parseImportedArticle(markdownContent: string): ImportedArticleContent {
  const lines = markdownContent.replace(/\r\n/g, '\n').split('\n')
  let cursor = lines.findIndex((line) => line.trim())
  let suggestedTitle = ''
  let suggestedSummary = ''

  if (cursor >= 0) {
    const firstLine = lines[cursor].trim()
    const heading = /^#\s+(.+)$/.exec(firstLine)
    const isPlainTitle = !/^(?:>|[-*+]\s|\d+\.\s|```|---+$|\|)/.test(firstLine)
    if (heading || isPlainTitle) {
      suggestedTitle = (heading?.[1] || firstLine).trim()
      lines.splice(cursor, 1)
    }
  }

  cursor = lines.findIndex((line) => line.trim())
  if (cursor >= 0 && /^>/.test(lines[cursor].trim())) {
    const quoteLines: string[] = []
    let end = cursor
    while (end < lines.length && /^\s*>/.test(lines[end])) {
      const text = lines[end].replace(/^\s*>\s?/, '').trim()
      if (text) quoteLines.push(text)
      end += 1
    }
    suggestedSummary = quoteLines.join(' ')
    lines.splice(cursor, end - cursor)
  }

  return {
    title: suggestedTitle,
    summary: suggestedSummary,
    content: lines.join('\n').replace(/^\s+/, '')
  }
}
