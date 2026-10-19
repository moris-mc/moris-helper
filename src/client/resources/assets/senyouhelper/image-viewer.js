(()=>{
 if(window.morisImage)return;
 let image=document.querySelector('video')||document.querySelector('img');
 const host=/^(static[0-9]*\.klipy\.com|media\.tenor\.com|c\.tenor\.com|(?:i|media[0-9]*)\.giphy\.com|images-ext-[0-9]+\.discordapp\.net|media\.discordapp\.net|cdn\.discordapp\.com)$/;
 const share=/^(www\.)?(klipy\.com|tenor\.com|giphy\.com)$/.test(location.hostname);
 if(share){
  if(!window.morisShareImage){let best=null,rank=-1;
   for(const meta of document.querySelectorAll('meta[property="og:image"],meta[property="og:video"],meta[property="og:video:secure_url"]')){try{const target=new URL(meta.content,location.href);if(target.protocol!=='https:'||target.username||target.password||(target.port&&target.port!=='443')||!host.test(target.hostname))continue;const score=/\.mp4$/i.test(target.pathname)?4:/\.gif$/i.test(target.pathname)?3:/\.webm$/i.test(target.pathname)?2:1;if(score>rank){best=target;rank=score;}}catch(e){}}
   if(!best)return;const preview=document.createElement(/\.(mp4|webm)$/i.test(best.pathname)?'video':'img');preview.src=best.href;window.morisShareImage=preview;
  }
  image=window.morisShareImage;
 }
 const video=image&&image.tagName==='VIDEO';
 if(video){image.muted=true;image.autoplay=true;image.loop=true;image.play().catch(()=>{});}
 const naturalWidth=image&&(video?image.videoWidth:image.naturalWidth),naturalHeight=image&&(video?image.videoHeight:image.naturalHeight);
 if(!image||!naturalWidth||!(video?image.readyState>=2:image.complete))return;
 if(share)document.body.replaceChildren(image);
 if(video){image.muted=true;image.loop=true;image.controls=false;image.play().catch(()=>{});}
 document.documentElement.style.cssText='width:100%;height:100%;margin:0;overflow:hidden;background:#16191f';
 document.body.style.cssText='width:100%;height:100%;margin:0;overflow:hidden;background:#16191f';
 image.style.cssText='position:absolute;left:0;top:0;max-width:none;max-height:none;margin:0;transform-origin:0 0;image-rendering:auto;user-select:none;cursor:grab';
 image.width=naturalWidth;image.height=naturalHeight;image.draggable=false;
 const state={zoom:1,x:0,y:0,fitMode:true};let drag=null;
 const fitZoom=()=>Math.min(1,innerWidth/naturalWidth,innerHeight/naturalHeight);
 const paint=()=>{image.style.transform=`translate(${state.x}px,${state.y}px) scale(${state.zoom})`;};
 const reset=original=>{state.fitMode=!original;state.zoom=original?1:fitZoom();state.x=(innerWidth-naturalWidth*state.zoom)/2;state.y=(innerHeight-naturalHeight*state.zoom)/2;paint();};
 const zoom=(factor,px=innerWidth/2,py=innerHeight/2)=>{const next=Math.max(Math.max(.005,fitZoom()/10),Math.min(16,state.zoom*factor));state.x=px-(px-state.x)*next/state.zoom;state.y=py-(py-state.y)*next/state.zoom;state.zoom=next;state.fitMode=false;paint();};
 window.morisImage={state,fit:()=>reset(false),original:()=>reset(true),zoom};
 addEventListener('wheel',e=>{e.preventDefault();zoom(Math.exp(-e.deltaY*.0015),e.clientX,e.clientY);},{passive:false});
 addEventListener('pointerdown',e=>{if(e.button!==0)return;e.preventDefault();drag={mx:e.clientX,my:e.clientY,x:state.x,y:state.y};image.style.cursor='grabbing';},true);
 addEventListener('pointermove',e=>{if(!drag)return;state.x=drag.x+e.clientX-drag.mx;state.y=drag.y+e.clientY-drag.my;paint();});
 addEventListener('pointerup',()=>{drag=null;image.style.cursor='grab';});
 addEventListener('click',e=>{e.preventDefault();e.stopImmediatePropagation();},true);
 addEventListener('dragstart',e=>e.preventDefault());
 addEventListener('resize',()=>{if(state.fitMode)reset(false);else paint();});
 reset(false);
})();
