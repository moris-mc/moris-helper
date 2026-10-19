const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
class Element{
 constructor(tag){this.tag=tag;this.children=[];this.style={};this.textContent='';}
 append(...nodes){this.children.push(...nodes)}
 replaceChildren(...nodes){this.children=[...nodes]}
}
const document={documentElement:new Element('html'),head:new Element('head'),body:new Element('body'),createElement:t=>new Element(t),createTextNode:text=>({tag:'#text',textContent:text})};
const context={document,URL,Date,location:{hash:""},scrollY:123};context.window=context;
const script=fs.readFileSync('src/client/resources/assets/senyouhelper/discord-feed.js','utf8');vm.runInNewContext(script,context);
const feed=context.morisFeed;
assert.equal(feed.safeURL('javascript:alert(1)'),null);assert.equal(feed.safeURL('http://insecure.test/x'),null);assert.equal(feed.safeURL('https://name:password@example.org'),null);
const signed='https://cdn.discordapp.com/attachments/a/b/image.png?ex=123&hm=signed';assert.equal(feed.safeURL(signed),signed);
feed.render({status:'僅供閱讀',messages:[{id:'1',author:'<img onerror=alert(1)>',time:'2026-10-17T12:00:00Z',text:'**粗體** __底線__ ~~刪除~~\n> 引用 <@&42>\n<script>alert(1)</script>',roles:{42:'小森友'},embeds:[],attachments:[{image:true,url:signed}]}]});
const walk=n=>[n,...n.children?.flatMap(walk)||[]],all=walk(document.body),tags=all.map(e=>e.tag);
for(const tag of ['strong','u','s','blockquote','img'])assert(tags.includes(tag));
assert(!tags.includes('script'));assert.equal(tags.filter(t=>t==='input').length,1);assert(!tags.includes('form'));
assert(all.some(e=>e.className==='role'&&e.textContent==='@小森友'));
const thumbnail=all.find(e=>e.tag==='img'&&e.src===signed);thumbnail.onclick();
const overlay=document.body.children.find(e=>e.id==='overlay');assert.equal(overlay.style.display,'block');assert.equal(overlay.children[1].src,signed);assert.equal(overlay.children[1].style.width,'');overlay.children[0].onclick();assert.equal(overlay.style.display,'none');
vm.runInNewContext(script,context);assert.equal(context.morisFeed,feed);
feed.render({status:'空頻道',messages:[]});assert.equal(document.body.children[1].children.length,0);
console.log('PASS: read-only rich text, named roles, safe DOM text, URL restrictions, signed original image overlay, empty state, repeat initialization.');

feed.render({status:'格式檢查',messages:[{id:'9',author:'安平',time:'2026-10-18T00:00:00Z',text:'# 大標題\n## 中標題\n### 小標題\n> 第一行\n> 第二行\n\n> 分開的引用\n😀 <:moris:1514571219590971422> <a:dance:42>\n[【點擊我開啟】](https://www.mcmoris.net/news/更新-83)\n[其他連結](https://discord.com/channels/1/2)',roles:{},attachments:[],embeds:[]}]});
const rich=walk(document.body),richTags=rich.map(n=>n.tag);
for(const tag of ['h1','h2','h3'])assert(richTags.includes(tag));
assert.equal(rich.filter(n=>n.tag==='blockquote').length,2);
assert.equal(rich.find(n=>n.tag==='blockquote').children.length,2);
assert(rich.some(n=>n.className==='unicode-emoji'&&n.children.some(i=>i.alt==='😀')));
const custom=rich.filter(n=>n.className==='custom-emoji');assert.equal(custom.length,2);assert(custom[0].src.includes('/1514571219590971422.png?'));assert(custom[1].src.includes('/42.gif?'));
assert.equal(feed.officialURL('https://mcmoris.net.evil.test/wiki'),null);assert.equal(feed.officialURL('https://mcmoris.net:444/wiki'),null);
const official=rich.find(n=>n.tag==='a'&&n.textContent==='【點擊我開啟】');assert(official);official.onclick({preventDefault(){}});assert(context.location.hash.startsWith('moris-open=https%3A%2F%2Fwww.mcmoris.net'));assert(context.location.hash.endsWith('&scroll=123'));
const before=context.location.hash;rich.find(n=>n.tag==='a'&&n.textContent==='其他連結').onclick({preventDefault(){}});assert.equal(context.location.hash,before);
console.log('PASS: three heading levels, joined/split quotes, Unicode and custom animated emoji, masked links, official-site-only native window handoff.');

feed.render({status:'跨行格式',messages:[{id:'10',author:'安平',time:'2026-10-18T00:00:00Z',text:'**也在這邊重申：\n\n目前只是「暫時」停止，不代表之後不會再開啟。**\n-# 小提醒：26.1 ~ 26.2\n`程式碼`\n🉑 🔶 👨‍👩‍👧‍👦',roles:{},attachments:[],embeds:[]}]});
const latest=walk(document.body);const strong=latest.filter(n=>n.tag==='strong');assert.equal(strong.length,2);assert(strong[0].children.some(n=>n.textContent?.includes('也在這邊重申')));assert(strong[1].children.some(n=>n.textContent?.includes('目前只是')));
assert(latest.some(n=>n.className==='subtext'&&n.children.some(n=>n.textContent?.startsWith('小提醒'))));
const unicode=latest.filter(n=>n.className==='custom-emoji unicode-image');assert.equal(unicode.length,3);assert(unicode[0].src.includes('/1f251.png'));assert(unicode[1].src.includes('/1f536.png'));assert(unicode[2].src.includes('200d'));assert.equal(latest.filter(n=>n.tag==='code').length,1);
console.log('PASS: bold across blank lines, Discord subtext, code styling structure, emoji images including astral and ZWJ sequences.');

feed.render({status:'搜尋測試',more:true,messages:[{id:'11',author:'安平',time:'2026-10-18T00:00:00Z',text:'銀行功能更新',unread:true,roles:{},embeds:[],attachments:[]},{id:'12',author:'森友',time:'2026-10-18T00:00:00Z',text:'活動公告',unread:false,roles:{},embeds:[],attachments:[]}]});
const input=walk(document.body).find(n=>n.tag==='input');input.value='銀行';input.oninput();assert.equal(document.body.children[1].children.length,1);const badge=walk(document.body).find(n=>n.className==='unread');assert(badge);badge.onclick();assert.equal(context.location.hash,'moris-read=11');
feed.render({status:'標記更新',messages:[{id:'11',author:'安平',time:'2026-10-18T00:00:00Z',text:'銀行功能更新',unread:false,roles:{},embeds:[],attachments:[]}]});assert.equal(input.value,'銀行');assert(!walk(document.body).some(n=>n.className==='unread'));
input.value='不存在';input.oninput();assert(walk(document.body).some(n=>n.className==='no-results'));input.value='';input.oninput();assert.equal(document.body.children[1].children.length,1);
console.log('PASS: Chinese search, scoped results, explicit empty results, per-announcement read handoff and search text retained on refresh.');
