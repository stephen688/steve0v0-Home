export interface RichNode {
  name?: string;
  type?: string;
  text?: string;
  attrs?: Record<string, string>;
  children?: RichNode[];
}

export interface MarkdownResult {
  nodes: RichNode[];
  imageUrls: string[];
  links: { text: string; url: string }[];
  outline: { index: string; title: string }[];
}

const paragraphStyle = 'display:block;margin:0 0 48rpx;color:#35302c;font-size:34rpx;line-height:2;letter-spacing:0.5rpx;';

function primaryHeadingNode(source: string, imageUrls: string[], links: { text: string; url: string }[]): RichNode {
  return {
    name: 'div',
    attrs: { style: 'display:block;margin:72rpx 0 44rpx;color:#211e1c;font-size:48rpx;font-weight:900;line-height:1.42;letter-spacing:-0.4rpx;' },
    children: inlineNodes(source, imageUrls, links)
  };
}

function secondHeadingNode(source: string, imageUrls: string[], links: { text: string; url: string }[]): RichNode {
  const displaySource = source
    .replace(/[*_`]/g, '')
    .replace(/^\s*[一二三四五六七八九十\d]+[.、)）]\s*/, '')
    .trim();
  return {
    name: 'div',
    attrs: { style: 'display:block;margin:60rpx 0 28rpx;color:#211f1d;font-size:38rpx;font-weight:900;line-height:1.4;letter-spacing:-0.2rpx;' },
    children: [
      { name: 'span', attrs: { style: 'display:inline;margin-right:12rpx;color:#f45b12;font-size:39rpx;font-weight:400;line-height:1;' }, children: [textNode('┃')] },
      { name: 'span', attrs: { style: 'display:inline;color:#211f1d;font-size:38rpx;font-weight:900;line-height:1.4;' }, children: [textNode(displaySource)] }
    ]
  };
}

function middleHeadingNode(source: string, imageUrls: string[], links: { text: string; url: string }[]): RichNode {
  const displaySource = source.replace(/[*_`]/g, '').replace(/^\s*\d+[.、)）]\s*/, '').trim();
  return {
    name: 'div',
    attrs: { style: 'display:block;margin:50rpx 0 24rpx;padding:0 2rpx 12rpx;color:#3b332d;font-size:34rpx;font-weight:800;line-height:1.55;border-bottom:2rpx solid #ead9ca;' },
    children: inlineNodes(displaySource, imageUrls, links)
  };
}

function smallHeadingNode(source: string, imageUrls: string[], links: { text: string; url: string }[]): RichNode {
  return {
    name: 'div',
    attrs: { style: 'display:block;margin:48rpx 0 22rpx;color:#3b3531;font-size:35rpx;font-weight:800;line-height:1.65;' },
    children: inlineNodes(source, imageUrls, links)
  };
}

function dividerNode(): RichNode {
  return {
    name: 'div',
    attrs: { style: 'display:flex;align-items:center;justify-content:center;margin:58rpx 0;color:#d5ad88;font-size:24rpx;letter-spacing:12rpx;' },
    children: [textNode('• • •')]
  };
}

function shortStrongLabel(line: string) {
  const match = /^\s*(\*\*|__)(.+)\1\s*$/.exec(line);
  if (!match) return '';
  const label = match[2].trim();
  const compactLength = label.replace(/\s/g, '').length;
  return compactLength <= 22 && !/[。！？.!?]$/.test(label) ? label : '';
}

function isMiddleStrongLabel(label: string, groupStart: boolean) {
  const compact = label.replace(/\s/g, '');
  return groupStart || /^\d+[.、)）]/.test(compact) || compact.length >= 10;
}

function textNode(text: string): RichNode {
  return { type: 'text', text };
}

function pushPlainText(nodes: RichNode[], value: string) {
  const parts = value.split('\n');
  parts.forEach((part, index) => {
    if (index) nodes.push({ name: 'br' });
    if (part) nodes.push(textNode(part));
  });
}

function inlineNodes(source: string, imageUrls: string[], links: { text: string; url: string }[]): RichNode[] {
  const nodes: RichNode[] = [];
  const pattern = /!\[([^\]]*)\]\((https?:\/\/[^\s)]+)\)|\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)|`([^`]+)`|\*\*([^*]+)\*\*|__([^_]+)__|\*([^*]+)\*|_([^_]+)_/g;
  let cursor = 0;
  let match: RegExpExecArray | null;
  while ((match = pattern.exec(source))) {
    if (match.index > cursor) pushPlainText(nodes, source.slice(cursor, match.index));
    if (match[2]) {
      imageUrls.push(match[2]);
      nodes.push({ name: 'img', attrs: { src: match[2], alt: match[1], style: 'display:block;width:100%;margin:26rpx 0;border-radius:18rpx;' } });
    } else if (match[4]) {
      links.push({ text: match[3], url: match[4] });
      nodes.push({ name: 'a', attrs: { href: match[4], style: 'color:#c65712;text-decoration:underline;' }, children: [textNode(match[3])] });
    } else if (match[5]) {
      nodes.push({ name: 'code', attrs: { style: 'display:inline;margin:0 5rpx;padding:3rpx 10rpx;background:#f3e9df;color:#9b4d13;border-radius:7rpx;font-size:29rpx;' }, children: [textNode(match[5])] });
    } else if (match[6] || match[7]) {
      nodes.push({ name: 'strong', attrs: { style: 'display:inline;margin:0 5rpx;padding:0 3rpx;border-bottom:4rpx solid #f0c79f;color:#292421;font-weight:800;' }, children: [textNode(match[6] || match[7] || '')] });
    } else {
      nodes.push({ name: 'em', children: [textNode(match[8] || match[9] || '')] });
    }
    cursor = match.index + match[0].length;
  }
  if (cursor < source.length) pushPlainText(nodes, source.slice(cursor));
  return nodes.length ? nodes : [textNode('')];
}

function isHorizontalRule(line: string) {
  return /^\s*([-*_])(?:\s*\1){2,}\s*$/.test(line);
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
    || /^(#{1,6})\s+/.test(line)
    || /^\s*>/.test(line)
    || /^\s*(?:[-*+] |\d+\. )/.test(line)
    || isHorizontalRule(line)
    || (line.includes('|') && isTableDivider(next));
}

function renderTable(headers: string[], rows: string[][], imageUrls: string[], links: { text: string; url: string }[]): RichNode {
  return {
    name: 'div',
    attrs: { style: 'display:block;margin:22rpx 0 54rpx;' },
    children: [
      ...rows.map((row, rowIndex) => ({
        name: 'div',
        attrs: { style: `display:block;padding:24rpx 26rpx;border:2rpx solid #eadfd4;border-radius:18rpx;background:#fffdf9;${rowIndex < rows.length - 1 ? 'margin-bottom:18rpx;' : ''}` },
        children: row.map((cell, cellIndex) => ({
          name: 'div',
          attrs: { style: `display:block;${cellIndex > 0 ? 'margin-top:18rpx;padding-top:18rpx;border-top:2rpx solid #f1e8df;' : ''}` },
          children: [
            ...(headers[cellIndex] ? [{ name: 'span', attrs: { style: 'display:block;margin-bottom:6rpx;color:#a06a42;font-size:21rpx;font-weight:700;' }, children: inlineNodes(headers[cellIndex], imageUrls, links) } as RichNode] : []),
            { name: 'span', attrs: { style: `display:block;color:#403934;font-size:${cellIndex === 0 ? '31rpx' : '29rpx'};font-weight:${cellIndex === 0 ? '800' : '400'};line-height:1.85;` }, children: inlineNodes(cell, imageUrls, links) }
          ]
        }))
      }))
    ]
  };
}

export function markdownToNodes(markdown: string): MarkdownResult {
  const nodes: RichNode[] = [];
  const imageUrls: string[] = [];
  const links: { text: string; url: string }[] = [];
  const outline: { index: string; title: string }[] = [];
  const lines = String(markdown || '').replace(/\r\n/g, '\n').replace(/<[^>]*>/g, '').split('\n');
  let index = 0;
  let chapterIndex = 0;
  let strongGroupStart = true;

  while (index < lines.length) {
    const line = lines[index];
    if (!line.trim()) {
      index += 1;
      continue;
    }

    if (/^\s*```/.test(line)) {
      const language = line.replace(/^\s*```/, '').trim().toUpperCase() || 'CODE';
      const code: string[] = [];
      index += 1;
      while (index < lines.length && !/^\s*```/.test(lines[index])) {
        code.push(lines[index]);
        index += 1;
      }
      if (index < lines.length) index += 1;
      nodes.push({
        name: 'div',
        attrs: { style: 'display:block;overflow:hidden;margin:16rpx 0 48rpx;border-radius:20rpx;background:#2e2926;' },
        children: [
          { name: 'div', attrs: { style: 'display:block;padding:14rpx 22rpx;border-bottom:2rpx solid rgba(255,255,255,0.08);color:#eab986;font-size:19rpx;font-weight:800;letter-spacing:3rpx;' }, children: [textNode(language)] },
          { name: 'pre', attrs: { style: 'display:block;overflow:auto;margin:0;padding:26rpx 24rpx;color:#fff7ed;font-size:25rpx;line-height:1.8;' }, children: [textNode(code.join('\n'))] }
        ]
      });
      continue;
    }

    const heading = /^(#{1,6})\s+(.+)$/.exec(line);
    if (heading) {
      const level = heading[1].length;
      if (level === 2) {
        chapterIndex += 1;
        strongGroupStart = true;
        const title = heading[2].replace(/[*_`]/g, '').trim();
        outline.push({ index: String(chapterIndex).padStart(2, '0'), title });
        nodes.push(secondHeadingNode(heading[2], imageUrls, links));
      } else if (level === 1) {
        nodes.push(primaryHeadingNode(heading[2], imageUrls, links));
        strongGroupStart = true;
      } else {
        nodes.push(middleHeadingNode(heading[2], imageUrls, links));
        strongGroupStart = false;
      }
      index += 1;
      continue;
    }

    const strongLabel = shortStrongLabel(line);
    if (strongLabel) {
      const isMiddle = isMiddleStrongLabel(strongLabel, strongGroupStart);
      nodes.push(isMiddle
        ? middleHeadingNode(strongLabel, imageUrls, links)
        : smallHeadingNode(strongLabel, imageUrls, links));
      strongGroupStart = false;
      index += 1;
      continue;
    }

    if (/^\s*>/.test(line)) {
      const quoteBlocks: string[][] = [];
      while (index < lines.length) {
        const currentBlock: string[] = [];
        while (index < lines.length && /^\s*>/.test(lines[index])) {
          currentBlock.push(lines[index].replace(/^\s*>\s?/, ''));
          index += 1;
        }
        if (currentBlock.some((quoteLine) => quoteLine.trim())) quoteBlocks.push(currentBlock);

        let nextQuoteIndex = index;
        while (nextQuoteIndex < lines.length && !lines[nextQuoteIndex].trim()) nextQuoteIndex += 1;
        const nextIsLabeledQuote = /^\s*>\s*(?:\*\*|__)/.test(lines[nextQuoteIndex] || '');
        if (!nextIsLabeledQuote) break;
        index = nextQuoteIndex;
      }

      const timelineGroups = quoteBlocks.map((currentBlock) => {
        const contentLines = currentBlock.filter((quoteLine) => quoteLine.trim());
        return {
          title: shortStrongLabel(contentLines[0] || ''),
          content: contentLines.slice(1).join('\n').trim()
        };
      });
      const isTimeline = timelineGroups.length >= 2 && timelineGroups.every((currentGroup) => currentGroup.title && currentGroup.content);

      if (isTimeline) {
        nodes.push({
          name: 'div',
          attrs: { style: 'display:block;margin:12rpx 0 56rpx;' },
          children: [
            ...timelineGroups.map((currentGroup, timelineIndex) => ({
              name: 'div',
              attrs: { style: `display:flex;align-items:flex-start;${timelineIndex < timelineGroups.length - 1 ? 'margin-bottom:22rpx;' : ''}` },
              children: [
                {
                  name: 'span',
                  attrs: { style: 'display:flex;align-items:center;justify-content:center;flex:none;width:52rpx;height:52rpx;margin:18rpx 18rpx 0 0;border:2rpx solid #e6c4a5;border-radius:50%;background:#fff5e9;color:#c26827;font-size:20rpx;font-weight:800;' },
                  children: [textNode(String(timelineIndex + 1).padStart(2, '0'))]
                },
                {
                  name: 'div',
                  attrs: { style: 'display:block;flex:1;padding:24rpx 26rpx 26rpx;border:2rpx solid #eadfd4;border-radius:20rpx;background:#fffdf9;box-shadow:0 10rpx 24rpx rgba(94,63,40,0.045);' },
                  children: [
                    { name: 'div', attrs: { style: 'display:block;margin-bottom:10rpx;color:#302a26;font-size:34rpx;font-weight:800;line-height:1.55;' }, children: inlineNodes(currentGroup.title, imageUrls, links) },
                    { name: 'div', attrs: { style: 'display:block;color:#62564d;font-size:31rpx;line-height:2;' }, children: inlineNodes(currentGroup.content, imageUrls, links) }
                  ]
                }
              ]
            }))
          ]
        });
      } else {
        const quoteChildren = quoteBlocks.map((currentBlock, groupIndex) => ({
          name: 'p',
          attrs: { style: `display:block;margin:0 0 ${groupIndex < quoteBlocks.length - 1 ? '16rpx' : '0'};line-height:1.9;` },
          children: inlineNodes(currentBlock.filter((quoteLine) => quoteLine.trim()).join('\n').trim(), imageUrls, links)
        } as RichNode));
        nodes.push({
          name: 'blockquote',
          attrs: { style: 'display:block;margin:14rpx 0 50rpx;padding:30rpx 30rpx 28rpx;border-left:7rpx solid #e78532;border-radius:0 20rpx 20rpx 0;background:#f8f0e7;color:#51473f;font-size:33rpx;line-height:2;' },
          children: [
            { name: 'span', attrs: { style: 'display:block;margin-bottom:14rpx;color:#c27335;font-size:46rpx;font-weight:900;line-height:0.65;' }, children: [textNode('“')] },
            ...quoteChildren
          ]
        });
      }
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
        name: 'div',
        attrs: { style: 'display:block;margin:8rpx 0 50rpx;padding:0 4rpx;' },
        children: [
          ...items.map((item, itemIndex) => ({
          name: 'div',
          attrs: { style: 'display:flex;align-items:flex-start;padding:11rpx 0;color:#38322e;font-size:33rpx;line-height:2;' },
          children: [
            {
              name: 'span',
              attrs: { style: `display:block;flex:none;min-width:${ordered ? '48rpx' : '34rpx'};color:#df650d;font-weight:800;` },
              children: [textNode(ordered ? `${itemIndex + 1}.` : '•')]
            },
            { name: 'span', attrs: { style: 'display:block;flex:1;' }, children: inlineNodes(item, imageUrls, links) }
          ]
          }))
        ]
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
      let nextIndex = index + 1;
      while (nextIndex < lines.length && !lines[nextIndex].trim()) nextIndex += 1;
      const beforeChapter = /^#{1,2}\s+/.test(lines[nextIndex] || '');
      if (!beforeChapter) nodes.push(dividerNode());
      strongGroupStart = true;
      index += 1;
      continue;
    }

    const imageOnly = /^!\[([^\]]*)\]\((https?:\/\/[^\s)]+)\)\s*$/.exec(line.trim());
    if (imageOnly) {
      imageUrls.push(imageOnly[2]);
      nodes.push({
        name: 'div',
        attrs: { style: 'display:block;margin:22rpx 0 54rpx;' },
        children: [
          { name: 'img', attrs: { src: imageOnly[2], alt: imageOnly[1], style: 'display:block;width:100%;border-radius:22rpx;background:#f2ebe4;' } },
          ...(imageOnly[1] ? [{ name: 'span', attrs: { style: 'display:block;margin-top:14rpx;color:#9a8778;font-size:23rpx;line-height:1.6;text-align:center;' }, children: [textNode(imageOnly[1])] } as RichNode] : [])
        ]
      });
      index += 1;
      continue;
    }

    const paragraph: string[] = [line];
    index += 1;
    while (index < lines.length && !startsBlock(lines, index)) {
      paragraph.push(lines[index]);
      index += 1;
    }
    const paragraphValue = paragraph.join(' ').trim();
    nodes.push({ name: 'p', attrs: { style: paragraphStyle }, children: inlineNodes(paragraphValue, imageUrls, links) });
  }

  return {
    nodes,
    imageUrls: Array.from(new Set(imageUrls)),
    links: links.filter((link, linkIndex, list) => list.findIndex((item) => item.url === link.url) === linkIndex),
    outline
  };
}
