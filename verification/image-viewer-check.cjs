const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const script = fs.readFileSync('src/client/resources/assets/senyouhelper/image-viewer.js', 'utf8');
const handlers = new Map();
const image = { complete:true, naturalWidth:4000, naturalHeight:3000, style:{} };
const context = { document:{ querySelector:()=>image, documentElement:{style:{}}, body:{style:{}} }, innerWidth:1200, innerHeight:800,
 addEventListener:(type,fn)=>{assert(!handlers.has(type));handlers.set(type,fn);}, Math, location:{hostname:"cdn.discordapp.com"} };
context.window=context;
vm.runInNewContext(script,context);
const view=context.morisImage;
assert(Math.abs(view.state.zoom-800/3000)<1e-10);
assert.equal(image.width,4000);assert.equal(image.height,3000);
view.original();assert.equal(view.state.zoom,1);
const oldX=view.state.x,oldY=view.state.y;
const beforeX=(200-oldX)/view.state.zoom,beforeY=(100-oldY)/view.state.zoom;
view.zoom(1.25,200,100);
assert(Math.abs((200-view.state.x)/view.state.zoom-beforeX)<1e-10);
assert(Math.abs((100-view.state.y)/view.state.zoom-beforeY)<1e-10);
handlers.get('pointerdown')({button:0,clientX:100,clientY:200,preventDefault(){}});
const dragX=view.state.x,dragY=view.state.y;
handlers.get('pointermove')({clientX:140,clientY:260});
assert.equal(view.state.x,dragX+40);assert.equal(view.state.y,dragY+60);
handlers.get('pointerup')();
view.fit();assert(Math.abs(view.state.zoom-800/3000)<1e-10);
vm.runInNewContext(script,context); // Idempotent installation must not reset or duplicate handlers.
assert.equal(context.morisImage,view);
console.log('PASS: original image dimensions, fit, 1:1, cursor-anchored zoom, panning, repeat initialization.');

const clip={tagName:'VIDEO',videoWidth:640,videoHeight:360,readyState:4,style:{},play(){this.played=true;return {catch(){}};}};
const movieContext={document:{querySelector:selector=>selector==='video'?clip:null,documentElement:{style:{}},body:{style:{}}},location:{hostname:'media.tenor.com'},innerWidth:800,innerHeight:600,addEventListener(){},Math};movieContext.window=movieContext;
vm.runInNewContext(script,movieContext);assert.equal(clip.loop,true);assert.equal(clip.muted,true);assert.equal(clip.controls,false);assert.equal(clip.played,true);assert.equal(clip.width,640);
const shareClip={...clip,style:{},played:false};let selected;
const shareContext={document:{querySelector:()=>null,querySelectorAll:()=>[{content:'https://static2.klipy.com/poster.png'},{content:'https://static2.klipy.com/movie.mp4'}],createElement:tag=>{assert.equal(tag,'video');return shareClip;},documentElement:{style:{}},body:{style:{},replaceChildren:media=>{selected=media;}}},location:{hostname:'klipy.com',href:'https://klipy.com/gifs/sample'},URL,innerWidth:800,innerHeight:600,addEventListener(){},Math};shareContext.window=shareContext;
vm.runInNewContext(script,shareContext);assert.equal(selected,shareClip);assert.equal(shareClip.src,'https://static2.klipy.com/movie.mp4');assert.equal(shareClip.loop,true);
console.log('PASS: muted looping video viewer and animated share source preferred over static poster.');
