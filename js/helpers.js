/* ═══ HELPERS — Toast, debounce engine, onboarding, nudge, header ═══ */
function showToast(msg,type){const t=document.getElementById("qaToast");t.textContent=msg;t.classList.add("show");if(type)t.style.background=type==="gold"?"var(--gold)":type==="dim"?"var(--border)":"";else t.style.background="";setTimeout(()=>{t.classList.remove("show");t.style.background=""},2000)}

// ─── Debounce Session Engine ───
const DEBOUNCE_MS=30000; // 30 seconds
let _pendingSessions={}; // keyed by bookId: {startPage, endPage, timer, countdownInterval, countdownLeft}

function _updateNRDisplay(bk){
  const pg=bk.pages||1,cur=bk.currentPage||0,pct=Math.min(100,Math.round(cur/pg*100));
  const col=CAT_COLORS[bk.category]||CAT_COLORS.Other;
  const pctEl=document.getElementById("nrPct_"+bk.id);if(pctEl)pctEl.textContent=pct+"%";
  const fillEl=document.getElementById("nrFill_"+bk.id);if(fillEl)fillEl.style.width=pct+"%";
  const stepper=document.querySelector(`.nr-stepper[data-book="${bk.id}"]`);
  if(stepper){const lbl=stepper.querySelector(".nr-val span");if(lbl)lbl.textContent=cur;const inp=stepper.querySelector(".nr-val input");if(inp)inp.value=cur}
}

function _updatePendingUI(bookId){
  const el=document.getElementById("nrPending_"+bookId);if(!el)return;
  const ps=_pendingSessions[bookId];
  if(!ps){el.innerHTML="";return}
  const pages=ps.endPage-ps.startPage;
  const pctLeft=Math.round((ps.countdownLeft/DEBOUNCE_MS)*100);
  if(pages>0)el.innerHTML=`<span class="pos">+${pages}p pending</span> <span class="countdown-bar"><span class="countdown-bar-inner" style="width:${pctLeft}%"></span></span>`;
  else el.innerHTML=`<span class="cancelled">back to start \u2014 will cancel</span> <span class="countdown-bar"><span class="countdown-bar-inner" style="width:${pctLeft}%"></span></span>`;
}

function _commitPending(bookId){
  const ps=_pendingSessions[bookId];if(!ps)return;
  clearTimeout(ps.timer);clearInterval(ps.countdownInterval);
  const pages=ps.endPage-ps.startPage;
  const bk=data.books.find(b=>b.id===bookId);
  if(pages>0&&bk){
    if(!bk.sessions)bk.sessions=[];
    bk.sessions.push({id:uid(),date:new Date().toISOString(),startPage:ps.startPage,endPage:ps.endPage,duration:0,notes:""});
    bk.updatedAt=new Date().toISOString();save();
    showToast(`Logged: pg ${ps.startPage} \u2192 ${ps.endPage}  (+${pages}p)`);
  }else if(pages<=0){
    showToast("Session cancelled","dim");
  }
  delete _pendingSessions[bookId];
  _updatePendingUI(bookId);
}

function _resetPendingTimer(bookId){
  const ps=_pendingSessions[bookId];if(!ps)return;
  clearTimeout(ps.timer);clearInterval(ps.countdownInterval);
  ps.countdownLeft=DEBOUNCE_MS;
  ps.countdownInterval=setInterval(()=>{ps.countdownLeft=Math.max(0,ps.countdownLeft-100);_updatePendingUI(bookId)},100);
  ps.timer=setTimeout(()=>_commitPending(bookId),DEBOUNCE_MS);
}

function nrMovePage(bookId,newPage){
  const bk=data.books.find(b=>b.id===bookId);if(!bk)return;
  const pg=bk.pages||9999;newPage=Math.max(0,Math.min(pg,newPage));
  if(newPage===bk.currentPage)return;
  const ps=_pendingSessions[bookId];
  if(!ps){
    if(newPage>bk.currentPage){
      _pendingSessions[bookId]={startPage:bk.currentPage,endPage:newPage,timer:null,countdownInterval:null,countdownLeft:DEBOUNCE_MS};
      bk.currentPage=newPage;bk.updatedAt=new Date().toISOString();save();
      _updateNRDisplay(bk);_resetPendingTimer(bookId);_updatePendingUI(bookId);
    }else{
      bk.currentPage=newPage;bk.updatedAt=new Date().toISOString();save();_updateNRDisplay(bk);
    }
  }else{
    bk.currentPage=newPage;bk.updatedAt=new Date().toISOString();save();_updateNRDisplay(bk);
    if(bk.currentPage<=ps.startPage){ps.endPage=ps.startPage}
    else{ps.endPage=bk.currentPage}
    _resetPendingTimer(bookId);_updatePendingUI(bookId);
  }
  // Book completion
  if(bk.currentPage>=pg){bk.status="completed";bk.endDate=new Date().toISOString().slice(0,10);bk.updatedAt=new Date().toISOString();save();_commitPending(bookId);showToast(`Finished "${bk.title}"! \u{1F389}`,"gold");setTimeout(()=>{renderShelf();updateHeader()},500);return}
  updateHeader();
}

// ─── Onboarding ───
let nudgeDismissed=false;
function checkOnboarding(){
  if(localStorage.getItem("rl-onboarded"))return;
  if(data.books.length>0){localStorage.setItem("rl-onboarded","1");return}
  // Inject onboarding overlay
  const ob=document.createElement("div");ob.id="onboarding";ob.className="onboarding-overlay";
  ob.innerHTML=`<div class="onb-slide active"><div class="onb-icon">\u{1F4DA}</div><h2 class="onb-title">Your Personal Library</h2><p class="onb-desc">Book spines on wooden shelves, color-coded by category. Tap any spine to explore. Gold edges mark your favorites.</p><div class="onb-dots"><span class="onb-dot active"></span><span class="onb-dot"></span><span class="onb-dot"></span></div><button class="onb-btn" id="onbNext0">Next</button><button class="onb-skip" id="onbSkip">Skip intro</button></div><div class="onb-slide"><div class="onb-icon">\u23F1</div><h2 class="onb-title">Track Your Reading</h2><p class="onb-desc">Log sessions with a built-in timer. See your progress, pace, and streaks on the Timeline. Quick-update pages right from the shelf.</p><div class="onb-dots"><span class="onb-dot"></span><span class="onb-dot active"></span><span class="onb-dot"></span></div><button class="onb-btn" id="onbNext1">Next</button><button class="onb-skip" id="onbSkip1">Skip intro</button></div><div class="onb-slide"><div class="onb-icon">\u{1F31F}</div><h2 class="onb-title">Review Your Year</h2><p class="onb-desc">Analytics, category breakdowns, reading habits, and a Year in Review that celebrates your progress.</p><div class="onb-dots"><span class="onb-dot"></span><span class="onb-dot"></span><span class="onb-dot active"></span></div><button class="onb-btn" id="onbStart">Start Fresh</button><button class="onb-demo" id="onbDemo">Load Demo Library (12 books)</button></div>`;
  document.body.appendChild(ob);
  function goSlide(n){ob.querySelectorAll(".onb-slide").forEach((s,i)=>{s.classList.toggle("active",i===n)})}
  function finish(loadDemo){
    if(loadDemo){data=makeDemoData();save();pickRandomQuote()}
    localStorage.setItem("rl-onboarded","1");ob.remove();renderAll();
  }
  document.getElementById("onbNext0").onclick=()=>goSlide(1);
  document.getElementById("onbNext1").onclick=()=>goSlide(2);
  document.getElementById("onbSkip").onclick=()=>finish(false);
  document.getElementById("onbSkip1").onclick=()=>finish(false);
  document.getElementById("onbStart").onclick=()=>finish(false);
  document.getElementById("onbDemo").onclick=()=>finish(true);
}

// ─── Nudge Banner ───
function getNudge(){
  if(nudgeDismissed)return null;
  const reading=getReading();if(!reading.length)return null;
  const b=reading[0],pages=b.pages||1,cur=b.currentPage||0,remaining=pages-cur,pct=Math.min(100,Math.round(cur/pages*100));
  if(pct<5)return null; // don't nudge if barely started
  const sesPages=(b.sessions||[]).reduce((s,se)=>s+Math.max(0,(se.endPage||0)-(se.startPage||0)),0);
  const daysReading=b.startDate?daysBetween(b.startDate,new Date().toISOString()):0;
  const pgDay=daysReading>0?sesPages/daysReading:0;
  const estDays=pgDay>0?Math.ceil(remaining/pgDay):0;
  let sub="";
  if(remaining<=50)sub=`Only ${remaining} pages left \u2014 finish it today?`;
  else if(estDays>0)sub=`At your pace, ~${estDays} day${estDays!==1?"s":""} to finish`;
  else sub=`${remaining} pages remaining`;
  return{bookId:b.id,title:b.title,pct,sub,icon:pct>=75?"\u{1F525}":pct>=50?"\u{1F4D6}":"\u2615"};
}

// ─── Quick-add helper ───
// ─── Header ───
function updateHeader(){
  const c=getCompleted().length,g=getGoal(),pct=Math.min(100,Math.round(c/g*100)),streak=getDayStreak();
  const R=11,C=2*Math.PI*R;
  let h=`<div class="goal-pill"><svg width="28" height="28" viewBox="0 0 28 28" style="transform:rotate(-90deg)"><circle cx="14" cy="14" r="${R}" fill="none" stroke="#282218" stroke-width="3"/><circle cx="14" cy="14" r="${R}" fill="none" stroke="#8B9E72" stroke-width="3" stroke-dasharray="${(pct/100)*C} ${C}" stroke-linecap="round"/></svg><span class="goal-pill-text">${c}<span>/${g}</span></span></div>`;
  if(streak.current>0)h+=`<div class="streak-pill">\u{1F525} ${streak.current}d streak</div>`;
  document.getElementById("hdrPills").innerHTML=h;
}

