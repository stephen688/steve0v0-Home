export interface RichNode {
  name?: string;
  type?: string;
  text?: string;
  attrs?: Record<string, string>;
  children?: RichNode[];
  blockType?: 'heading2' | 'quote' | 'table';
  blockNodes?: RichNode[];
  blockHeaders?: MarkdownCell[];
  blockRows?: MarkdownRow[];
}

export interface MarkdownCell {
  nodes: RichNode[];
}

export interface MarkdownRow {
  cells: MarkdownCell[];
}

export interface MarkdownBlock {
  type: 'rich' | 'heading2' | 'quote' | 'table';
  nodes: RichNode[];
  headers?: MarkdownCell[];
  rows?: MarkdownRow[];
}

export interface MarkdownResult {
  nodes: RichNode[];
  blocks: MarkdownBlock[];
  imageUrls: string[];
  links: { text: string; url: string }[];
  outline: { index: string; title: string }[];
}

const COLORS = {
  foreground: '#24211f',
  primary: '#d86a00',
  primaryForeground: '#f8f4ed',
  muted: '#ede6db',
  mutedForeground: '#756656',
  border: '#d9cfc0'
};

const BODY_FONT = '-apple-system,BlinkMacSystemFont,"Segoe UI","Noto Sans SC","PingFang SC","Microsoft YaHei",sans-serif';
const MONO_FONT = 'ui-monospace,"SF Mono",Consolas,monospace';
const PARAGRAPH_STYLE = `display:block;margin:0;color:${COLORS.foreground};font-family:${BODY_FONT};font-size:32rpx;font-weight:400;letter-spacing:0.4rpx;line-height:2;text-align:left;`;

function textNode(text: string): RichNode {
  return { type: 'text', text };
}

function pushPlainText(nodes: RichNode[], value: string) {
  if (value) nodes.push(textNode(value));
}

function inlineNodes(source: string, imageUrls: string[], links: { text: string; url: string }[]): RichNode[] {
  const nodes: RichNode[] = [];
  const pattern = /!\[([^\]]*)\]\(([^)\s]+)\)|\[([^\]]+)\]\(([^)\s]+)\)|`([^`]+)`|\*\*(\S(?:[^*]*?\S)?)\*\*|__(\S(?:[^_]*?\S)?)__|\*([^*\s](?:[^*]*?[^*\s])?)\*|_([^_\s](?:[^_]*?[^_\s])?)_/g;
  let cursor = 0;
  let match: RegExpExecArray | null;

  while ((match = pattern.exec(source))) {
    if (match.index > cursor) pushPlainText(nodes, source.slice(cursor, match.index));
    if (match[2]) {
      imageUrls.push(match[2]);
      nodes.push({
        name: 'img',
        attrs: {
          src: match[2],
          alt: match[1],
          style: 'display:block;max-width:100%;height:auto;margin:0 auto;border-radius:20rpx;'
        }
      });
    } else if (match[4]) {
      links.push({ text: match[3], url: match[4] });
      nodes.push({
        name: 'a',
        attrs: { href: match[4], style: `color:${COLORS.primary};text-decoration:underline;` },
        children: [textNode(match[3])]
      });
    } else if (match[5]) {
      nodes.push({
        name: 'code',
        attrs: { style: `color:${COLORS.primary};font-family:${MONO_FONT};font-size:0.85em;` },
        children: [textNode(match[5])]
      });
    } else if (match[6] || match[7]) {
      nodes.push({ name: 'strong', attrs: { style: 'font-weight:700;' }, children: [textNode(match[6] || match[7] || '')] });
    } else {
      nodes.push({ name: 'em', attrs: { style: 'font-style:italic;' }, children: [textNode(match[8] || match[9] || '')] });
    }
    cursor = match.index + match[0].length;
  }

  if (cursor < source.length) pushPlainText(nodes, source.slice(cursor));
  return nodes.length ? nodes : [textNode('')];
}

function inlineLines(lines: string[], imageUrls: string[], links: { text: string; url: string }[]) {
  const nodes: RichNode[] = [];
  lines.forEach((line, index) => {
    if (index) nodes.push({ name: 'br' });
    nodes.push(...inlineNodes(line.trim(), imageUrls, links));
  });
  return nodes;
}

function isHorizontalRule(line: string) {
  return /^\s*([-*_])(?:\s*\1){2,}\s*$/.test(line);
}

function blankLineNode(): RichNode {
  return {
    name: 'div',
    attrs: { style: 'display:block;width:100%;height:24rpx;font-size:1rpx;line-height:1rpx;' },
    children: [textNode('\u00a0')]
  };
}

function isTableDivider(line: string) {
  return /^\s*\|?\s*:?-{3,}:?\s*(?:\|\s*:?-{3,}:?\s*)+\|?\s*$/.test(line);
}

function tableCells(line: string) {
  return line.trim().replace(/^\|/, '').replace(/\|$/, '').split('|').map((cell) => cell.trim());
}

function startsBlock(lines: string[], index: number) {
  const line = lines[index] || '';
  const next = lines[index + 1] || '';
  return !line.trim()
    || /^\s*```/.test(line)
    || /^\s{0,3}#{1,6}\s+/.test(line)
    || /^\s*>/.test(line)
    || /^\s*(?:[-*+] |\d+\. )/.test(line)
    || isHorizontalRule(line)
    || (line.includes('|') && isTableDivider(next));
}

function headingStyle(level: number) {
  const base = `display:block;color:#1f1f1f;font-family:${BODY_FONT};font-weight:700;letter-spacing:0;line-height:1.4;`;
  if (level === 1) return `${base}margin:38rpx 0 0;font-size:48rpx;`;
  if (level === 3) return `${base}margin:28rpx 0 0;font-size:30rpx;`;
  return `${base}margin:24rpx 0 0;font-size:28rpx;`;
}

function renderHeading(level: number, title: string, imageUrls: string[], links: { text: string; url: string }[]): RichNode {
  const titleNodes = inlineNodes(title, imageUrls, links);
  if (level !== 2) {
    return { name: `h${level}`, attrs: { style: headingStyle(level) }, children: titleNodes };
  }

  return {
    name: 'h2',
    blockType: 'heading2',
    blockNodes: titleNodes,
    attrs: {
      style: `display:block;margin:38rpx 0 0;color:#1f1f1f;font-family:${BODY_FONT};font-size:44rpx;font-weight:700;letter-spacing:0;line-height:1.4;`
    },
    children: [
      {
        name: 'span',
        attrs: { style: 'display:inline-block;width:6rpx;height:46rpx;border-radius:3rpx;background:#ff8a00;background-image:linear-gradient(180deg,#ffb02e 0%,#ff8a00 48%,#ef6500 100%);font-size:1rpx;line-height:1rpx;vertical-align:-7rpx;' },
        children: [textNode('\u00a0')]
      },
      textNode('\u00a0'),
      ...titleNodes
    ]
  };
}

function renderTable(headers: string[], rows: string[][], imageUrls: string[], links: { text: string; url: string }[]): RichNode {
  const columnWidth = `${100 / Math.max(headers.length, 1)}%`;
  const cellStyle = `width:${columnWidth};box-sizing:border-box;padding:14rpx 12rpx;border:2rpx solid ${COLORS.border};color:${COLORS.foreground};font-size:24rpx;line-height:1.55;text-align:left;vertical-align:top;white-space:normal;word-break:break-word;overflow-wrap:anywhere;`;
  const blockHeaders = headers.map((header) => ({ nodes: inlineNodes(header, imageUrls, links) }));
  const blockRows = rows.map((row) => ({
    cells: headers.map((_, columnIndex) => ({ nodes: inlineNodes(row[columnIndex] || '', imageUrls, links) }))
  }));
  return {
    name: 'table',
    blockType: 'table',
    blockHeaders,
    blockRows,
    attrs: { style: 'display:table;width:100%;margin:0;border-collapse:collapse;border-spacing:0;table-layout:fixed;' },
    children: [
      {
        name: 'thead',
        attrs: { style: 'display:table-header-group;' },
        children: [{
          name: 'tr',
          attrs: { style: 'display:table-row;' },
          children: blockHeaders.map((cell) => ({
            name: 'th',
            attrs: { style: `${cellStyle}background:${COLORS.muted};font-weight:700;` },
            children: cell.nodes
          }))
        }]
      },
      {
        name: 'tbody',
        attrs: { style: 'display:table-row-group;' },
        children: blockRows.map((row) => ({
          name: 'tr',
          attrs: { style: 'display:table-row;' },
          children: row.cells.map((cell) => ({
            name: 'td',
            attrs: { style: cellStyle },
            children: cell.nodes
          }))
        }))
      }
    ]
  };
}

function createBlocks(nodes: RichNode[]): MarkdownBlock[] {
  return nodes.map((node) => {
    if (node.blockType === 'heading2') return { type: 'heading2', nodes: node.blockNodes || [] };
    if (node.blockType === 'quote') return { type: 'quote', nodes: node.blockNodes || [] };
    if (node.blockType === 'table') {
      return { type: 'table', nodes: [], headers: node.blockHeaders || [], rows: node.blockRows || [] };
    }
    return { type: 'rich', nodes: [node] };
  });
}

export function markdownToNodes(markdown: string): MarkdownResult {
  const nodes: RichNode[] = [];
  const imageUrls: string[] = [];
  const links: { text: string; url: string }[] = [];
  const outline: { index: string; title: string }[] = [];
  const lines = String(markdown || '').replace(/\r\n/g, '\n').split('\n');
  let index = 0;

  while (index < lines.length) {
    const line = lines[index];
    if (!line.trim()) {
      while (index < lines.length && !lines[index].trim()) index += 1;
      if (nodes.length && index < lines.length) nodes.push(blankLineNode());
      continue;
    }

    if (/^\s*```/.test(line)) {
      const code: string[] = [];
      index += 1;
      while (index < lines.length && !/^\s*```/.test(lines[index])) {
        code.push(lines[index]);
        index += 1;
      }
      if (index < lines.length) index += 1;
      nodes.push({
        name: 'pre',
        attrs: { style: `display:block;overflow-x:auto;margin:0;padding:28rpx;border-radius:16rpx;background:${COLORS.foreground};color:${COLORS.primaryForeground};font-family:${MONO_FONT};font-size:26rpx;line-height:1.6;white-space:pre;` },
        children: [{ name: 'code', children: [textNode(code.join('\n'))] }]
      });
      continue;
    }

    const heading = /^\s{0,3}(#{1,6})\s+(.+)$/.exec(line);
    if (heading) {
      const level = heading[1].length;
      const title = heading[2].trim();
      if (level === 2) outline.push({ index: String(outline.length + 1), title: title.replace(/[*_`]/g, '') });
      nodes.push(renderHeading(level, title, imageUrls, links));
      index += 1;
      continue;
    }

    if (/^\s*>/.test(line)) {
      const quoteLines: string[] = [];
      while (index < lines.length && /^\s*>/.test(lines[index])) {
        quoteLines.push(lines[index].replace(/^\s*>\s?/, ''));
        index += 1;
      }
      const quoteNodes = inlineLines(quoteLines, imageUrls, links);
      nodes.push({
        name: 'div',
        blockType: 'quote',
        blockNodes: quoteNodes,
        attrs: {
          style: 'display:block;box-sizing:border-box;margin:0;padding:2rpx 2rpx 2rpx 8rpx;border-radius:16rpx;background:#ff8a00;background-image:linear-gradient(135deg,#ffb02e 0%,#ff8a00 46%,#ef6500 100%);'
        },
        children: [{
          name: 'div',
          attrs: {
            style: `display:block;margin:0;padding:24rpx 28rpx;border-radius:12rpx;background:#f7f2eb;color:${COLORS.foreground};font-family:${BODY_FONT};font-size:32rpx;font-weight:400;letter-spacing:0.2rpx;line-height:1.8;`
          },
          children: quoteNodes
        }]
      });
      continue;
    }

    const listMatch = /^\s*(?:([-*+]) |(\d+)\. )(.+)$/.exec(line);
    if (listMatch) {
      const ordered = Boolean(listMatch[2]);
      const items: string[] = [];
      while (index < lines.length) {
        const itemMatch = /^\s*(?:([-*+]) |(\d+)\. )(.+)$/.exec(lines[index]);
        if (!itemMatch || Boolean(itemMatch[2]) !== ordered) break;
        items.push(itemMatch[3]);
        index += 1;
      }
      nodes.push({
        name: ordered ? 'ol' : 'ul',
        attrs: { style: `display:block;margin:0;padding-left:48rpx;color:${COLORS.primary};font-size:32rpx;font-weight:400;letter-spacing:0.4rpx;line-height:2;` },
        children: items.map((item) => ({
          name: 'li',
          attrs: { style: 'display:list-item;margin:14rpx 0;padding-left:8rpx;' },
          children: [{
            name: 'span',
            attrs: { style: `color:${COLORS.foreground};` },
            children: inlineNodes(item, imageUrls, links)
          }]
        }))
      });
      continue;
    }

    if (line.includes('|') && isTableDivider(lines[index + 1] || '')) {
      const headers = tableCells(line);
      const rows: string[][] = [];
      index += 2;
      while (index < lines.length && lines[index].includes('|') && lines[index].trim()) {
        rows.push(tableCells(lines[index]));
        index += 1;
      }
      nodes.push(renderTable(headers, rows, imageUrls, links));
      continue;
    }

    if (isHorizontalRule(line)) {
      nodes.push({ name: 'hr', attrs: { style: `display:block;margin:56rpx 0;border:0;border-top:2rpx solid ${COLORS.border};` } });
      index += 1;
      continue;
    }

    const paragraph: string[] = [line];
    index += 1;
    while (index < lines.length && !startsBlock(lines, index)) {
      paragraph.push(lines[index]);
      index += 1;
    }
    const hasIndent = /^ {2}(?=\S)/.test(paragraph[0]);
    nodes.push({
      name: 'p',
      attrs: { style: `${PARAGRAPH_STYLE}${hasIndent ? 'text-indent:2em;' : ''}` },
      children: inlineLines(paragraph, imageUrls, links)
    });
  }

  return {
    nodes,
    blocks: createBlocks(nodes),
    imageUrls: Array.from(new Set(imageUrls)),
    links: links.filter((link, linkIndex, list) => list.findIndex((item) => item.url === link.url) === linkIndex),
    outline
  };
}
