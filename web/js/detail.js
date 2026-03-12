/* ═══ DETAIL — Book detail view, always inline editable ═══ */
// ══════ DETAIL VIEW ══════
function openDetail(id){detailBook=data.books.find(b=>b.id===id);if(!detailBook)return;try{renderDetail()}catch(e){document.getElementById("detailView").innerHTML=`<div style="padding:40px;color:red;font-family:var(--ui)"><p>Error: ${e.message}</p><pre>${e.stack}</pre><button onclick="closeDetail()" style="margin-top:20px;padding:10px 20px">Back</button></div>`}document.getElementById("app").style.display="none";document.getElementById("detailView").classList.add("active");document.getElementById("bottomNav").classList.add("hidden");document.getElementById("fab").classList.add("hidden");window.scrollTo(0,0)}
function closeDetail(){document.getElementById("detailView").classList.remove("active");document.getElementById("app").style.display="";document.getElementById("bottomNav").classList.remove("hidden");document.getElementById("fab").classList.remove("hidden");renderAll()}

function renderDetail(){
const bk=detailBook;if(!bk)return;
const lc=CAT_COLORS[bk.category]||CAT_COLORS.Other,dur=daysBetween(bk.startDate,bk.endDate);
const quotes=bk.quotes||[],themes=bk.themes||[],sessions=bk.sessions||[];

// ─── Hero Banner ───
let act="";if(bk.status==="reading")act+=`<button class="dbtn green" data-action="complete">&#10003; Complete</button>`;if(bk.status==="want-to-read")act+=`<button class="dbtn blue" data-action="start">&#9654; Start Reading</button>`;if(bk.status==="completed")act+=`<button class="dbtn blue-o" data-action="reread">&#9654; Re-read</button>`;act+=`<button class="dbtn ghost" data-action="delete">&#128465;</button>`;
let meta="";if(bk.pages)meta+=`<span class="detail-meta-item"><strong>${bk.pages}</strong> pages</span>`;if(bk.startDate)meta+=`<span class="detail-meta-item">${fmtShort(bk.startDate)}</span>`;if(bk.endDate)meta+=`<span class="detail-meta-item">&mdash; ${fmtShort(bk.endDate)}</span>`;if(dur)meta+=`<span class="detail-meta-item">(${dur}d)</span>`;
const starsHTML=[1,2,3,4,5].map(s=>`<span class="detail-star-tap${s<=(bk.rating||0)?" on":""}" data-star="${s}">${s<=(bk.rating||0)?"\u2605":"\u2606"}</span>`).join("");

let html=`<div class="detail-banner"><div class="detail-banner-bg" style="background:linear-gradient(180deg,${lc}30 0%,${lc}12 40%,var(--bg) 100%)"></div><div class="detail-banner-top"><button class="detail-back" id="detailBack"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M19 12H5"/><path d="M12 19l-7-7 7-7"/></svg> Back</button></div><div class="detail-banner-content"><div class="detail-pills"><span class="cat-pill" style="background:${lc}22;color:${lc}">${esc(bk.category)}</span><span class="status-badge ${bk.status}">${STATUS_LABELS[bk.status]}</span></div><h1 class="detail-title">${esc(bk.title)}</h1><p class="detail-author">${esc(bk.author)}</p><div class="detail-meta-row">${meta}<span class="detail-meta-item detail-stars-inline">${starsHTML}</span></div><div class="detail-actions" id="detailActions">${act}</div></div></div><div class="detail-body">`;

// ─── Status-aware body (always editable) ───

if(bk.status==="want-to-read"){
  if(bk.recommendedBy||bk.recommendationNote)html+=`<div class="rec-context">${bk.recommendedBy?`Recommended by <span class="rec-by">${esc(bk.recommendedBy)}</span>`:""}${bk.recommendationSource?`<span class="rec-source-pill">${esc(bk.recommendationSource)}</span>`:""}${bk.recommendationNote?`<p class="rec-note">${esc(bk.recommendationNote)}</p>`:""}</div>`;
  html+=`<div class="detail-section"><span class="detail-section-label">Why this book?</span><textarea class="refl-ta" id="notesTA" placeholder="Why did you add this? What do you hope to get from it?">${esc(bk.notes||"")}</textarea></div>`;
  html+=renderThemesSection(bk);
}

else if(bk.status==="reading"){
  const pg=bk.pages||1,cur=bk.currentPage||0,pct=Math.min(100,Math.round(cur/pg*100));
  html+=`<div class="detail-section"><div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px"><span class="detail-section-label">Progress</span><span style="font-family:var(--ui);font-size:14px;font-weight:600;color:var(--accent)">${pct}%</span></div><div style="height:12px;background:var(--border);border-radius:6px;overflow:hidden"><div style="height:100%;width:${pct}%;background:${lc};border-radius:6px;transition:width 0.3s"></div></div><p style="font-family:var(--ui);font-size:11px;color:var(--textD);margin-top:6px;text-align:center">pg ${cur} of ${pg}</p></div>`;
  // Timer
  const isThis=timerState.bookId===bk.id,isRun=isThis&&timerState.running&&!timerState.paused,isPau=isThis&&timerState.paused,el=isThis?timerState.elapsed:0,mins=Math.floor(el/60),secs=el%60,disp=String(mins).padStart(2,"0")+":"+String(secs).padStart(2,"0");
  html+=`<div class="session-timer"><p class="timer-label">Reading Timer</p><p class="timer-display timer-live">${disp}</p><div class="timer-btns">${isRun?`<button class="timer-btn pause" id="timerPause">Pause</button><button class="timer-btn stop" id="timerStop">Stop</button>`:isPau?`<button class="timer-btn start" id="timerResume">Resume</button><button class="timer-btn stop" id="timerStop">Stop</button>`:`<button class="timer-btn start" id="timerStart">Start</button>`}<button class="timer-btn log" id="logSessionBtn">Log Session</button></div><div id="sessionFormArea"></div></div>`;
  // Notes
  html+=`<div class="detail-section"><span class="detail-section-label">Notes</span><textarea class="refl-ta tall" id="notesTA" placeholder="Jot observations as you read\u2026">${esc(bk.notes||"")}</textarea></div>`;
  // Themes
  html+=renderThemesSection(bk);
  // Quotes
  html+=renderQuotesSection(bk);
  // Sessions
  if(sessions.length){html+=`<div class="detail-section"><span class="detail-section-label">Sessions</span><div class="session-list">${[...sessions].reverse().slice(0,5).map(s=>`<div class="session-item"><div class="session-info"><p class="session-date">${fmtDate(s.date)}</p><p class="session-detail">${s.startPage!=null&&s.endPage!=null?"p. "+s.startPage+" \u2192 "+s.endPage+" \u00B7 ":""}${s.duration||0} min</p></div><button class="session-del" data-sid="${s.id}">&times;</button></div>`).join("")}</div>${sessions.length>5?`<p style="font-family:var(--ui);font-size:11px;color:var(--textDD);margin-top:8px">${sessions.length} total sessions</p>`:""}</div>`}
}

else if(bk.status==="completed"||bk.status==="abandoned"){
  // Quotes
  html+=renderQuotesSection(bk);
  // My Takeaway
  html+=`<div class="detail-section"><span class="detail-section-label">My Takeaway</span><p style="font-family:var(--ui);font-size:11px;color:var(--textDD);margin-bottom:8px">What\u2019s the one thing from this book you\u2019d tell a friend?</p><textarea class="refl-ta" id="argTA" placeholder="The central idea\u2026">${esc(bk.coreArgument||"")}</textarea><textarea class="refl-ta" id="impactTA" placeholder="What shifted in your understanding?" style="margin-top:8px">${esc(bk.impact||"")}</textarea><textarea class="refl-ta tall" id="notesTA" placeholder="Other notes\u2026" style="margin-top:8px">${esc(bk.notes||"")}</textarea></div>`;
  // Themes
  html+=renderThemesSection(bk);
  // Sessions summary
  if(sessions.length){const totalMin=sessions.reduce((s,se)=>s+(se.duration||0),0);const totalPages=sessions.reduce((s,se)=>s+Math.max(0,(se.endPage||0)-(se.startPage||0)),0);
  html+=`<div class="detail-section"><span class="detail-section-label">Sessions</span><div style="font-family:var(--ui);font-size:13px;color:var(--textM)">${sessions.length} session${sessions.length!==1?'s':''}${totalMin?' \u00B7 '+Math.round(totalMin/60)+' hrs':''}${totalPages?' \u00B7 '+totalPages+' pages':''}</div></div>`}
}

html+=`</div>`;
document.getElementById("detailView").innerHTML=html;
bindDetailEvents();
}

function renderThemesSection(bk){
  const themes=bk.themes||[];
  let h=`<div class="detail-section"><span class="detail-section-label">Themes</span>`;
  h+=`<div class="themes-row" id="detailThemes">${themes.map(t=>`<span class="theme-tag">${esc(t)}<button data-theme="${esc(t)}">&times;</button></span>`).join("")}<input class="theme-input" id="themeInput" placeholder="Add theme\u2026"></div>`;
  return h+`</div>`;
}

function renderQuotesSection(bk){
  const quotes=bk.quotes||[];const lc=CAT_COLORS[bk.category]||CAT_COLORS.Other;
  let h=`<div class="detail-section"><div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px"><span class="detail-section-label">Saved Passages</span><button class="rbtn save" id="addQuoteBtn" style="font-size:11px;padding:4px 10px">+ Add</button></div>`;
  h+=`<div id="quoteForm" style="display:none"></div>`;
  if(quotes.length)h+=quotes.map(q=>`<div class="quote-item" style="border-left-color:${lc}60"><p class="quote-text">&ldquo;${esc(q.text)}&rdquo;</p><div class="quote-footer">${q.page?`<span class="quote-page">p. ${esc(q.page)}</span>`:""}<span></span><button class="quote-del" data-qid="${q.id}">&times;</button></div></div>`).join("");
  else h+=`<p class="refl-empty">Save memorable passages.</p>`;
  return h+`</div>`;
}

function _flashSave(el){if(!el)return;el.classList.remove('save-flash');void el.offsetWidth;el.classList.add('save-flash')}

function _autoSave(bk,sourceEl){
  const argTA=document.getElementById("argTA");if(argTA)bk.coreArgument=argTA.value.trim();
  const impTA=document.getElementById("impactTA");if(impTA)bk.impact=impTA.value.trim();
  const notesTA=document.getElementById("notesTA");if(notesTA)bk.notes=notesTA.value.trim();
  bk.updatedAt=new Date().toISOString();save();
  if(sourceEl)_flashSave(sourceEl);
}

function bindDetailEvents(){
const bk=detailBook;
document.getElementById("detailBack").onclick=()=>{_autoSave(bk);closeDetail()};
// Auto-save textareas on blur
document.querySelectorAll("#detailView textarea").forEach(ta=>ta.onblur=()=>_autoSave(bk,ta));
// Actions
document.querySelectorAll("#detailActions .dbtn").forEach(btn=>btn.onclick=()=>{_autoSave(bk);const a=btn.dataset.action;if(a==="complete"){bk.status="completed";bk.endDate=new Date().toISOString().slice(0,10);bk.currentPage=bk.pages||bk.currentPage;bk.updatedAt=new Date().toISOString();save();renderDetail()}else if(a==="start"){bk.status="reading";bk.startDate=bk.startDate||new Date().toISOString().slice(0,10);bk.endDate="";bk.updatedAt=new Date().toISOString();save();renderDetail()}else if(a==="reread"){bk.status="reading";bk.startDate=new Date().toISOString().slice(0,10);bk.endDate="";bk.currentPage=0;bk.updatedAt=new Date().toISOString();save();renderDetail()}else if(a==="delete"){showConfirm("Delete Book","Permanently remove?",()=>{data.books=data.books.filter(b=>b.id!==bk.id);save();closeDetail()})}});
// Inline star rating
document.querySelectorAll(".detail-stars-inline .detail-star-tap").forEach(s=>s.onclick=e=>{e.stopPropagation();const star=parseInt(s.dataset.star)||0;bk.rating=bk.rating===star?0:star;bk.updatedAt=new Date().toISOString();save();renderDetail()});
// Themes
document.querySelectorAll("#detailThemes .theme-tag button").forEach(b=>b.onclick=()=>{bk.themes=(bk.themes||[]).filter(t=>t!==b.dataset.theme);bk.updatedAt=new Date().toISOString();save();renderDetail()});
const ti=document.getElementById("themeInput");if(ti)ti.onkeydown=e=>{if(e.key==="Enter"&&ti.value.trim()){if(!bk.themes)bk.themes=[];if(!bk.themes.includes(ti.value.trim())){bk.themes.push(ti.value.trim());bk.updatedAt=new Date().toISOString();save();renderDetail()}}};
// Quotes
const aqb=document.getElementById("addQuoteBtn");if(aqb)aqb.onclick=()=>{const qf=document.getElementById("quoteForm");qf.style.display=qf.style.display==="none"?"block":"none";qf.innerHTML=`<div class="quote-form"><textarea id="qText" placeholder="Enter passage\u2026"></textarea><div class="quote-form-row"><input id="qPage" placeholder="Page #"><button class="rbtn save" id="qSave">Save</button><button class="rbtn cancel" id="qCancel">Cancel</button></div></div>`;document.getElementById("qSave").onclick=()=>{const t=document.getElementById("qText").value.trim();if(!t)return;if(!bk.quotes)bk.quotes=[];bk.quotes.push({id:uid(),text:t,page:document.getElementById("qPage").value.trim()||null,addedAt:new Date().toISOString()});bk.updatedAt=new Date().toISOString();save();renderDetail()};document.getElementById("qCancel").onclick=()=>{qf.style.display="none"}};
document.querySelectorAll(".quote-del").forEach(b=>b.onclick=e=>{e.stopPropagation();bk.quotes=(bk.quotes||[]).filter(q=>q.id!==b.dataset.qid);bk.updatedAt=new Date().toISOString();save();renderDetail()});
// Sessions delete
document.querySelectorAll(".session-del").forEach(b=>b.onclick=()=>{bk.sessions=(bk.sessions||[]).filter(s=>s.id!==b.dataset.sid);bk.updatedAt=new Date().toISOString();save();renderDetail()});
// Log session manually
const logBtn=document.getElementById("logSessionBtn");if(logBtn)logBtn.onclick=()=>{const area=document.getElementById("sessionFormArea");if(!area)return;if(area.innerHTML){area.innerHTML="";return}area.innerHTML=`<div class="session-form"><div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:8px"><div><p class="form-label">Date</p><input class="form-input" type="date" id="sfDate" value="${new Date().toISOString().slice(0,10)}"></div><div><p class="form-label">Duration (min)</p><input class="form-input" type="number" id="sfDur" placeholder="30"></div></div><div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-bottom:8px"><div><p class="form-label">Start Page</p><input class="form-input" type="number" id="sfStartP" placeholder="\u2014"></div><div><p class="form-label">End Page</p><input class="form-input" type="number" id="sfEndP" placeholder="\u2014"></div></div><div style="margin-bottom:8px"><p class="form-label">Notes</p><input class="form-input" id="sfNotes" placeholder="Session notes..."></div><div style="display:flex;gap:8px"><button class="rbtn save" id="sfSave">Save</button><button class="rbtn cancel" id="sfCancel">Cancel</button></div></div>`;document.getElementById("sfSave").onclick=()=>{const dur=parseInt(document.getElementById("sfDur").value)||0;if(!dur)return;if(!bk.sessions)bk.sessions=[];bk.sessions.push({id:uid(),date:document.getElementById("sfDate").value||new Date().toISOString().slice(0,10),startPage:parseInt(document.getElementById("sfStartP").value)||null,endPage:parseInt(document.getElementById("sfEndP").value)||null,duration:dur,notes:document.getElementById("sfNotes").value.trim()});const ep=parseInt(document.getElementById("sfEndP").value);if(ep)bk.currentPage=ep;bk.updatedAt=new Date().toISOString();save();renderDetail()};document.getElementById("sfCancel").onclick=()=>{area.innerHTML=""}};
// Detail timer
const timerStartBtn=document.getElementById("timerStart");if(timerStartBtn)timerStartBtn.onclick=()=>{if(timerState.intervalId)clearInterval(timerState.intervalId);timerState={running:true,paused:false,startTime:Date.now(),elapsed:0,intervalId:null,bookId:bk.id,_pauseAccum:0};timerState.intervalId=setInterval(()=>{timerState.elapsed=Math.floor((Date.now()-timerState.startTime)/1000)+(timerState._pauseAccum||0);document.querySelectorAll('.timer-live').forEach(el=>{const m=Math.floor(timerState.elapsed/60),s=timerState.elapsed%60;el.textContent=String(m).padStart(2,"0")+":"+String(s).padStart(2,"0")})},1000);renderDetail()};
const timerPauseBtn=document.getElementById("timerPause");if(timerPauseBtn)timerPauseBtn.onclick=()=>{if(timerState.intervalId)clearInterval(timerState.intervalId);timerState._pauseAccum=timerState.elapsed;timerState.paused=true;timerState.running=true;timerState.intervalId=null;renderDetail()};
const timerResumeBtn=document.getElementById("timerResume");if(timerResumeBtn)timerResumeBtn.onclick=()=>{if(timerState.intervalId)clearInterval(timerState.intervalId);timerState.startTime=Date.now();timerState.paused=false;timerState.intervalId=setInterval(()=>{timerState.elapsed=Math.floor((Date.now()-timerState.startTime)/1000)+(timerState._pauseAccum||0);document.querySelectorAll('.timer-live').forEach(el=>{const m=Math.floor(timerState.elapsed/60),s=timerState.elapsed%60;el.textContent=String(m).padStart(2,"0")+":"+String(s).padStart(2,"0")})},1000);renderDetail()};
const timerStopBtn=document.getElementById("timerStop");if(timerStopBtn)timerStopBtn.onclick=()=>{if(timerState.intervalId)clearInterval(timerState.intervalId);const durMins=Math.max(1,Math.round(timerState.elapsed/60));timerState={running:false,paused:false,startTime:null,elapsed:0,intervalId:null,bookId:null,_pauseAccum:0};if(!bk.sessions)bk.sessions=[];bk.sessions.push({id:uid(),date:new Date().toISOString().slice(0,10),startPage:null,endPage:null,duration:durMins,notes:"(timed)"});bk.updatedAt=new Date().toISOString();save();renderDetail()};
}
