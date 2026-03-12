/* ═══ TIMELINE — Focus hero, activity feed, Up Next, Gantt ═══ */
// ══════ TIMELINE ══════
function renderTimeline(){const el=document.getElementById("viewTimeline");const reading=getReading();let html='';
// ═══ Focus Hero: progress ring, last session, timer ═══
if(reading.length){const idx=(window._focusIdx||0)%reading.length;const book=reading[idx];const pages=book.pages||1,current=book.currentPage||0,pct=Math.min(100,Math.round(current/pages*100));const color=CAT_COLORS[book.category]||CAT_COLORS.Other;const r=90,circ=2*Math.PI*r,dashOff=circ-(pct/100)*circ;
const isTimerBook=timerState.bookId===book.id,isRunning=isTimerBook&&timerState.running&&!timerState.paused,isPaused=isTimerBook&&timerState.paused;
const elapsed=isTimerBook?timerState.elapsed:0,mm=Math.floor(elapsed/60),ss=elapsed%60,timeStr=String(mm).padStart(2,"0")+":"+String(ss).padStart(2,"0");
html+=`<div class="tlf-wrap">`;if(reading.length>1)html+=`<div class="tlf-nav"><button class="tlf-nav-btn tlf-prev">\u2190</button><span class="tlf-nav-count">${idx+1} of ${reading.length}</span><button class="tlf-nav-btn tlf-next">\u2192</button></div>`;
html+=`<div class="tlf-ring"><svg viewBox="0 0 200 200" width="200" height="200"><circle cx="100" cy="100" r="${r}" fill="none" stroke="var(--border)" stroke-width="8"/><circle cx="100" cy="100" r="${r}" fill="none" stroke="${color}" stroke-width="8" stroke-dasharray="${circ}" stroke-dashoffset="${dashOff}" stroke-linecap="round" transform="rotate(-90 100 100)" style="transition:stroke-dashoffset 0.8s"/></svg><div class="tlf-ring-inner"><p class="tlf-ring-pct" style="color:${color}">${pct}%</p><p class="tlf-ring-pages">${current} / ${pages}</p></div></div>`;
html+=`<p class="tlf-title" data-id="${book.id}">${esc(book.title)}</p><p class="tlf-author">${esc(book.author||"")}</p>`;
// Last session context
const lastSes=(book.sessions||[]).filter(s=>s.endPage>s.startPage).sort((a,b)=>new Date(b.date)-new Date(a.date))[0];
if(lastSes){const lp=Math.max(0,(lastSes.endPage||0)-(lastSes.startPage||0));const ld=lastSes.duration?lastSes.duration+' min':'';const ldate=lastSes.date?fmtShort(lastSes.date.slice(0,10)):'';html+=`<div class="tlf-last"><span class="tlf-last-icon">\u{1F4D6}</span><div class="tlf-last-body"><p class="tlf-last-label">Last session</p><p class="tlf-last-detail">${lp} pages${ld?' \u00B7 '+ld:''}</p><p class="tlf-last-sub">${ldate}${lastSes.startPage!=null?' \u00B7 pg '+lastSes.startPage+' \u2192 '+lastSes.endPage:''}</p></div></div>`}
// Timer + session save form
if(pendingSessionDur>0&&!timerState.running&&window._lastTimerBookId===book.id){
  html+=`<div class="tlf-session-form" data-book="${book.id}"><p style="font-family:var(--ui);font-size:14px;color:var(--accent);font-weight:600;margin-bottom:10px">Session Complete: ${pendingSessionDur} min</p><div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:8px"><div><p style="font-family:var(--ui);font-size:10px;color:var(--textD);text-transform:uppercase;margin-bottom:3px">Start Page</p><input class="tlf-page-input nr-st-start" type="number" placeholder="\u2014" style="width:100%"></div><div><p style="font-family:var(--ui);font-size:10px;color:var(--textD);text-transform:uppercase;margin-bottom:3px">End Page</p><input class="tlf-page-input nr-st-end" type="number" placeholder="\u2014" style="width:100%"></div></div><div style="margin-bottom:8px"><p style="font-family:var(--ui);font-size:10px;color:var(--textD);text-transform:uppercase;margin-bottom:3px">Notes</p><input class="tlf-page-input nr-st-notes" placeholder="Session notes..." style="width:100%"></div><div style="display:flex;gap:8px;justify-content:center"><button class="tlf-tbtn start nr-st-save" data-book="${book.id}">Save Session</button><button class="tlf-tbtn nr-st-skip" data-book="${book.id}" style="background:var(--border);color:var(--textM)">Skip</button></div></div>`;
}else{
  html+=`<div class="tlf-timer"><span class="tlf-timer-display timer-live">${timeStr}</span><div class="tlf-timer-btns">`;
  if(isRunning)html+=`<button class="tlf-tbtn pause" data-book="${book.id}">Pause</button><button class="tlf-tbtn stop" data-book="${book.id}">Stop</button>`;
  else if(isPaused)html+=`<button class="tlf-tbtn resume" data-book="${book.id}">Resume</button><button class="tlf-tbtn stop" data-book="${book.id}">Stop</button>`;
  else html+=`<button class="tlf-tbtn start" data-book="${book.id}">Start Reading</button>`;
  html+=`</div></div>`;
}
html+=`</div>`}else{html+=`<p class="tl-empty">No books in progress. Add a book with status \u201CReading\u201D to get started.</p>`}

// ═══ Activity Feed (plain, no weekly summaries) ═══
const today=new Date();today.setHours(0,0,0,0);const todayKey=today.toISOString().slice(0,10);
const events=[];data.books.forEach(b=>{if(b.startDate)events.push({type:'start',date:b.startDate,book:b});if(b.endDate&&b.status==="completed")events.push({type:'finish',date:b.endDate,book:b});(b.sessions||[]).forEach(s=>events.push({type:'session',date:s.date,book:b,session:s}));(b.quotes||[]).forEach(q=>{if(q.addedAt)events.push({type:'quote',date:q.addedAt,book:b,quote:q})})});events.sort((a,b)=>new Date(b.date)-new Date(a.date));
html+=`<hr class="tl-divider"><p class="tl-section-title">Activity</p>`;
if(events.length){const shown=events.slice(0,window._tlStreamCount||20);
const dayGroups=[];let lastDay='';shown.forEach(ev=>{const dk=new Date(ev.date).toISOString().slice(0,10);if(dk!==lastDay){dayGroups.push({key:dk,events:[]});lastDay=dk}dayGroups[dayGroups.length-1].events.push(ev)});
const yestD=new Date(today);yestD.setDate(yestD.getDate()-1);const yestKey=yestD.toISOString().slice(0,10);
dayGroups.forEach(g=>{
  let label=fmtShort(g.key);if(g.key===todayKey)label='<strong>Today</strong> \u00B7 '+label;else if(g.key===yestKey)label='<strong>Yesterday</strong> \u00B7 '+label;else label='<strong>'+label+'</strong>';
  html+=`<div class="tl-feed-day"><div class="tl-feed-day-label">${label}</div>`;g.events.forEach(ev=>{if(ev.type==='session'){const pages=Math.max(0,(ev.session.endPage||0)-(ev.session.startPage||0));let sub=[];if(ev.session.startPage!=null&&ev.session.endPage!=null)sub.push(`p.${ev.session.startPage} \u2192 ${ev.session.endPage}`);if(ev.session.duration)sub.push(ev.session.duration+'min');if(pages>0&&ev.session.duration>0)sub.push((pages/ev.session.duration).toFixed(1)+' pg/min');
  html+=`<div class="tl-fi session" data-id="${ev.book.id}"><div class="tl-fi-body"><p class="tl-fi-text">Read <strong>${esc(ev.book.title)}</strong>${pages>0?' \u2014 <span class="pg">'+pages+' pages</span>':''}</p>${sub.length?'<p class="tl-fi-sub">'+sub.join(' \u00B7 ')+'</p>':''}</div></div>`}
  else if(ev.type==='finish'){const dur=daysBetween(ev.book.startDate,ev.book.endDate);let meta=[];if(ev.book.pages)meta.push(ev.book.pages+' pages');if(dur)meta.push(dur+' days');html+=`<div class="tl-fi finish" data-id="${ev.book.id}"><div class="tl-fi-body"><p class="tl-fi-text">Finished <strong>${esc(ev.book.title)}</strong>${meta.length?' \u2014 '+meta.join(' in '):''}</p>${ev.book.rating?'<p class="tl-fi-stars">'+'\u2605'.repeat(ev.book.rating)+'</p>':''}</div></div>`}
  else if(ev.type==='start'){html+=`<div class="tl-fi start" data-id="${ev.book.id}"><div class="tl-fi-body"><p class="tl-fi-text">Started <strong>${esc(ev.book.title)}</strong>${ev.book.pages?' \u00B7 '+ev.book.pages+' pages':''}</p></div></div>`}
  else if(ev.type==='quote'){html+=`<div class="tl-fi quote" data-id="${ev.book.id}"><div class="tl-fi-body"><p class="tl-fi-text">Saved quote from <strong>${esc(ev.book.title)}</strong></p><p class="tl-fi-quote">\u201C${esc(ev.quote.text||'')}\u201D</p></div></div>`}});html+=`</div>`});
if(events.length>(window._tlStreamCount||20))html+=`<button class="tls-show-more" id="tlShowMore">Show more</button>`}else{html+=`<p class="tl-empty">No reading activity yet.</p>`}

// ═══ Up Next ═══
const wantToRead=data.books.filter(b=>b.status==='want-to-read').sort((a,b)=>{if(a.recommendedBy&&!b.recommendedBy)return -1;if(!a.recommendedBy&&b.recommendedBy)return 1;if((b.priority||0)!==(a.priority||0))return(b.priority||0)-(a.priority||0);return new Date(a.addedAt)-new Date(b.addedAt)});
if(wantToRead.length){const nx=wantToRead[0];const nxCol=CAT_COLORS[nx.category]||CAT_COLORS.Other;
html+=`<hr class="tl-divider"><div class="tl-next"><div class="tl-next-header"><span class="tl-next-icon">\u{1F4DA}</span><span class="tl-next-title">Up Next</span></div>`;
html+=`<div class="tl-next-card" data-id="${nx.id}"><div class="tl-next-spine" style="background:linear-gradient(180deg,${nxCol},${nxCol}80)"></div><div class="tl-next-info"><p class="tl-next-book-title">${esc(nx.title)}</p><p class="tl-next-author">${esc(nx.author||'')}${nx.pages?' \u00B7 '+nx.pages+' pages':''}</p>${nx.recommendedBy?`<p class="tl-next-meta">Recommended by <span class="rec">${esc(nx.recommendedBy)}</span>${nx.recommendationNote?' \u00B7 '+esc(nx.recommendationNote):''}</p>`:''}</div><button class="tl-next-start" data-id="${nx.id}">Start \u25B8</button></div></div>`}

el.innerHTML=html;
bindTimelineEvents(el);
}

function bindTimelineEvents(el){
  const prevBtn=el.querySelector('.tlf-prev');if(prevBtn)prevBtn.onclick=e=>{e.stopPropagation();window._focusIdx=((window._focusIdx||0)-1+getReading().length)%getReading().length;renderTimeline()};
  const nextBtn=el.querySelector('.tlf-next');if(nextBtn)nextBtn.onclick=e=>{e.stopPropagation();window._focusIdx=(window._focusIdx||0)+1;renderTimeline()};
  el.querySelectorAll('.tlf-title[data-id]').forEach(t=>t.onclick=()=>openDetail(t.dataset.id));
  // Timer: start
  el.querySelectorAll('.tlf-tbtn.start:not(.nr-st-save)').forEach(btn=>btn.onclick=e=>{e.stopPropagation();const bid=btn.dataset.book;window._lastTimerBookId=bid;if(timerState.intervalId)clearInterval(timerState.intervalId);timerState={running:true,paused:false,startTime:Date.now(),elapsed:0,intervalId:null,bookId:bid,_pauseAccum:0};timerState.intervalId=setInterval(()=>{timerState.elapsed=Math.floor((Date.now()-timerState.startTime)/1000)+(timerState._pauseAccum||0);document.querySelectorAll('.timer-live').forEach(e2=>{const m=Math.floor(timerState.elapsed/60),s=timerState.elapsed%60;e2.textContent=String(m).padStart(2,"0")+":"+String(s).padStart(2,"0")})},1000);renderTimeline()});
  // Timer: pause
  el.querySelectorAll('.tlf-tbtn.pause').forEach(btn=>btn.onclick=e=>{e.stopPropagation();if(timerState.intervalId)clearInterval(timerState.intervalId);timerState._pauseAccum=timerState.elapsed;timerState.paused=true;timerState.running=true;timerState.intervalId=null;renderTimeline()});
  // Timer: resume
  el.querySelectorAll('.tlf-tbtn.resume').forEach(btn=>btn.onclick=e=>{e.stopPropagation();if(timerState.intervalId)clearInterval(timerState.intervalId);timerState.startTime=Date.now();timerState.paused=false;timerState.intervalId=setInterval(()=>{timerState.elapsed=Math.floor((Date.now()-timerState.startTime)/1000)+(timerState._pauseAccum||0);document.querySelectorAll('.timer-live').forEach(e2=>{const m=Math.floor(timerState.elapsed/60),s=timerState.elapsed%60;e2.textContent=String(m).padStart(2,"0")+":"+String(s).padStart(2,"0")})},1000);renderTimeline()});
  // Timer: stop
  el.querySelectorAll('.tlf-tbtn.stop:not(.nr-st-skip)').forEach(btn=>btn.onclick=e=>{e.stopPropagation();if(timerState.intervalId)clearInterval(timerState.intervalId);pendingSessionDur=Math.max(1,Math.round(timerState.elapsed/60));window._lastTimerBookId=btn.dataset.book;timerState={running:false,paused:false,startTime:null,elapsed:0,intervalId:null,bookId:null,_pauseAccum:0};renderTimeline()});
  // Session save
  el.querySelectorAll('.nr-st-save').forEach(btn=>btn.onclick=e=>{e.stopPropagation();const bk=data.books.find(b=>b.id===btn.dataset.book);if(!bk)return;if(!bk.sessions)bk.sessions=[];const form=btn.closest('.tlf-session-form');let sp=parseInt(form.querySelector('.nr-st-start')?.value);sp=(isNaN(sp)||sp<0)?null:sp;let ep=parseInt(form.querySelector('.nr-st-end')?.value);ep=(isNaN(ep)||ep<0)?null:ep;if(sp!=null&&ep!=null&&ep<sp){const tmp=sp;sp=ep;ep=tmp}if(ep!=null&&bk.pages&&ep>bk.pages)ep=bk.pages;const notes=(form.querySelector('.nr-st-notes')?.value||'').trim();bk.sessions.push({id:uid(),date:new Date().toISOString().slice(0,10),startPage:sp,endPage:ep,duration:pendingSessionDur,notes});if(ep)bk.currentPage=ep;bk.updatedAt=new Date().toISOString();pendingSessionDur=0;window._lastTimerBookId=null;save();renderTimeline();updateHeader()});
  // Session skip
  el.querySelectorAll('.nr-st-skip').forEach(btn=>btn.onclick=e=>{e.stopPropagation();pendingSessionDur=0;window._lastTimerBookId=null;renderTimeline()});
  // Feed clicks
  el.querySelectorAll('.tl-fi[data-id]').forEach(item=>item.onclick=()=>openDetail(item.dataset.id));
  const showMore=document.getElementById('tlShowMore');if(showMore)showMore.onclick=()=>{window._tlStreamCount=(window._tlStreamCount||20)+20;renderTimeline()};
  // Up Next: Start button
  el.querySelectorAll('.tl-next-start').forEach(btn=>btn.onclick=e=>{e.stopPropagation();const bk=data.books.find(b=>b.id===btn.dataset.id);if(!bk)return;bk.status='reading';bk.startDate=new Date().toISOString().slice(0,10);bk.updatedAt=new Date().toISOString();save();showToast(`Started "${bk.title}"`);renderTimeline();renderShelf();updateHeader()});
  el.querySelectorAll('.tl-next-card').forEach(card=>card.onclick=()=>openDetail(card.dataset.id));
}
