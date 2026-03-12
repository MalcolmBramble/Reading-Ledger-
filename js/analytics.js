/* ═══ ANALYTICS — DNA summary, stats, streak ring, categories, theme cloud, goals ═══ */
// ══════ ANALYTICS ══════
function renderAnalytics(){
const comp=getCompleted(),goal=getGoal(),pct=Math.min(100,Math.round(comp.length/goal*100)),totalPages=comp.reduce((s,b)=>s+(b.pages||0),0);
const avgDays=(()=>{const ds=comp.filter(b=>b.startDate&&b.endDate).map(b=>daysBetween(b.startDate,b.endDate));return ds.length?Math.round(ds.reduce((a,c)=>a+c,0)/ds.length):0})();
const allQuotes=[];data.books.forEach(b=>(b.quotes||[]).forEach(q=>allQuotes.push(q)));
const annotated=data.books.filter(b=>b.notes||b.coreArgument||b.impact||(b.quotes&&b.quotes.length>0)).length;
const sk=getDayStreak();

// Build theme data (used by DNA and theme cloud)
const allThemes={};data.books.forEach(b=>(b.themes||[]).forEach(t=>{const key=t.toLowerCase();if(!allThemes[key])allThemes[key]={name:t,books:[]};allThemes[key].books.push(b)}));
const themeEntries=Object.values(allThemes).sort((a,b)=>b.books.length-a.books.length);

// Category data
const catData=CATEGORIES.map(c=>({name:c,value:comp.filter(b=>b.category===c).length})).filter(c=>c.value>0).sort((a,b)=>b.value-a.value);

let html='';

// ═══ Reading DNA ═══
if(comp.length){
  const topCats=catData.slice(0,2).map(c=>c.name);
  const topThemeNames=themeEntries.slice(0,3).map(t=>t.name);
  let dna=`You\u2019ve completed <strong>${comp.length}</strong> book${comp.length!==1?'s':''} this year`;
  if(topCats.length)dna+=`, mostly <span class="dna-accent">${topCats.join('</span> and <span class="dna-accent">')}</span>`;
  dna+='.';
  if(sk.current>0)dna+=` You\u2019re on a <span class="dna-accent">${sk.current}-day streak</span>`;
  if(sk.best>sk.current)dna+=` \u2014 your best was ${sk.best}`;
  if(sk.current>0)dna+='.';
  if(topThemeNames.length)dna+=` Top themes: ${topThemeNames.map(t=>`<span class="dna-accent">${esc(t)}</span>`).join(', ')}.`;
  html+=`<div class="dna"><p class="dna-text">${dna}</p></div>`;
}

// ═══ Stat Cards ═══
html+=`<div class="mini-stats"><div class="mini-stat"><p class="mini-stat-label">Completed</p><p class="mini-stat-value">${comp.length}</p><p class="mini-stat-sub">of ${goal}</p></div><div class="mini-stat"><p class="mini-stat-label">Avg Pace</p><p class="mini-stat-value">${avgDays?avgDays+"d":"\u2014"}</p><p class="mini-stat-sub">per book</p></div><div class="mini-stat"><p class="mini-stat-label">Pages</p><p class="mini-stat-value">${totalPages.toLocaleString()}</p></div><div class="mini-stat"><p class="mini-stat-label">Annotated</p><p class="mini-stat-value">${annotated}</p></div><div class="mini-stat"><p class="mini-stat-label">Quotes</p><p class="mini-stat-value">${allQuotes.length}</p></div></div>`;

// ═══ Annual Progress ═══
html+=`<div class="panel"><div class="panel-title">Annual Progress</div><div class="progress-row"><div class="progress-track"><div class="progress-fill" style="width:${pct}%"></div></div><span class="progress-pct">${pct}%</span></div></div>`;

// ═══ Reading Patterns (streak ring + week strip only) ═══
const aRMax=Math.max(sk.best,7),aRPct=Math.min(100,Math.round(sk.current/aRMax*100)),aRR=50,aRC=2*Math.PI*aRR,aRO=aRC-(aRPct/100)*aRC;
html+=`<div class="panel"><div class="panel-title">Reading Patterns</div><div class="patterns-top"><div class="sr-ring-wrap"><svg viewBox="0 0 120 120" width="100" height="100"><circle cx="60" cy="60" r="${aRR}" fill="none" stroke="var(--border)" stroke-width="8"/><circle cx="60" cy="60" r="${aRR}" fill="none" stroke="var(--accent)" stroke-width="8" stroke-dasharray="${aRC}" stroke-dashoffset="${aRO}" stroke-linecap="round" transform="rotate(-90 60 60)"/></svg><div class="sr-ring-inner"><span class="sr-ring-num">${sk.current}</span><span class="sr-ring-label">day${sk.current!==1?'s':''}</span></div></div><div class="sr-details"><div class="sr-detail"><span class="sr-detail-label">Current streak</span><span class="sr-detail-val accent">${sk.current} day${sk.current!==1?'s':''}</span></div><div class="sr-detail"><span class="sr-detail-label">Best streak</span><span class="sr-detail-val">${sk.best} day${sk.best!==1?'s':''}</span></div><div class="sr-detail"><span class="sr-detail-label">This month</span><span class="sr-detail-val">${sk.monthRead} / ${sk.monthDays} days</span></div><div class="sr-detail"><span class="sr-detail-label">This week</span><span class="sr-detail-val accent">${sk.weekRead} / 7 days</span></div></div></div>`;
const todayKey=new Date().toISOString().slice(0,10);const dowVal=new Date().getDay()===0?6:new Date().getDay()-1;const weekStart=new Date();weekStart.setHours(0,0,0,0);weekStart.setDate(weekStart.getDate()-dowVal);
const dayLabels=['M','T','W','T','F','S','S'];let twC='',twL='';for(let i=0;i<7;i++){const wd=new Date(weekStart);wd.setDate(wd.getDate()+i);const wk=wd.toISOString().slice(0,10);twC+=`<div class="sr-tw-day${sk.sessionDays[wk]?' read':''}${wk===todayKey?' today':''}"></div>`;twL+=`<span class="sr-tw-label">${dayLabels[i]}</span>`}
html+=`<div class="sr-this-week">${twC}</div><div class="sr-tw-labels">${twL}</div></div>`;

// ═══ Categories ═══
if(catData.length)html+=`<div class="panel"><div class="panel-title">Categories</div><div class="pie-row"><div class="pie-wrap"><canvas id="pieChart"></canvas></div><div class="cat-legend">${catData.map(c=>`<div class="cat-legend-item"><div class="cat-legend-dot" style="background:${CAT_COLORS[c.name]}"></div><span class="cat-legend-name">${c.name}</span><span class="cat-legend-count">${c.value}</span><div class="cat-legend-bar"><div class="cat-legend-bar-fill" style="width:${(c.value/comp.length)*100}%;background:${CAT_COLORS[c.name]}"></div></div></div>`).join("")}</div></div></div>`;

// ═══ Theme Cloud ═══
if(themeEntries.length){
  const maxT=Math.max(...themeEntries.map(t=>t.books.length));
  html+=`<div class="panel"><div class="panel-title">Themes</div><div class="theme-cloud" id="themeCloud">${themeEntries.map(t=>{
    const size=t.books.length>=maxT?'lg':t.books.length>1?'md':'sm';
    return`<div class="theme-pill ${size}" data-theme="${esc(t.name)}"><span class="theme-pill-name">${esc(t.name)}</span><span class="theme-pill-count">${t.books.length}</span></div>`;
  }).join('')}</div><div class="theme-books" id="themeBooks"></div><p class="theme-auto-note">Themes are auto-tagged from book metadata when you search and add.</p></div>`;
}

// ═══ Goals & Challenges ═══
const catGoals=data.settings.goals.categories||{},challenges=data.settings.goals.challenges||[];
if(Object.keys(catGoals).length||challenges.length){html+=`<div class="panel"><div class="panel-title">Goals &amp; Challenges</div>`;
if(Object.keys(catGoals).length){html+=`<div class="goal-panel-section"><p class="goal-panel-section-title">Category Goals</p>`;Object.entries(catGoals).forEach(([cat,target])=>{const prog=getCategoryProgress(cat);if(!prog)return;const color=CAT_COLORS[cat]||CAT_COLORS.Other;html+=`<div style="margin-bottom:10px"><div style="display:flex;justify-content:space-between;font-family:var(--ui);font-size:12px;margin-bottom:3px"><span style="color:var(--textM)">${cat}</span><span style="color:var(--textD)">${prog.count}/${prog.target}</span></div><div class="challenge-progress-bar"><div class="challenge-progress-fill" style="width:${prog.pct}%;background:${color}"></div></div></div>`});html+=`</div>`}
if(challenges.length){html+=`<div class="goal-panel-section"><p class="goal-panel-section-title">Challenges</p>`;challenges.forEach(ch=>{const prog=getChallengeProgress(ch);html+=`<div class="challenge-card${prog.done?" challenge-done":""}"><p class="challenge-label">${esc(ch.label)}</p><p class="challenge-meta">${ch.type} \u00B7 ${prog.current}/${ch.target}</p><div class="challenge-progress-bar"><div class="challenge-progress-fill" style="width:${prog.pct}%"></div></div><p class="challenge-pct">${prog.pct}%</p></div>`});html+=`</div>`}
html+=`</div>`}

document.getElementById("viewAnalytics").innerHTML=html;
requestAnimationFrame(()=>{
  if(catData.length)drawPie(catData,comp.length);
  // Theme cloud click handlers
  const tc=document.getElementById("themeCloud");const tb=document.getElementById("themeBooks");
  if(tc)tc.querySelectorAll(".theme-pill").forEach(pill=>pill.onclick=()=>{
    const name=pill.dataset.theme;const entry=themeEntries.find(t=>t.name===name);if(!entry)return;
    const wasActive=pill.classList.contains("active");
    tc.querySelectorAll(".theme-pill").forEach(p=>p.classList.remove("active"));
    if(wasActive){tb.classList.remove("show");tb.innerHTML="";return}
    pill.classList.add("active");tb.classList.add("show");
    tb.innerHTML=`<p class="theme-active-label">${entry.books.length} book${entry.books.length!==1?'s':''} tagged \u201C${esc(entry.name)}\u201D</p>${entry.books.map(b=>{
      const col=CAT_COLORS[b.category]||CAT_COLORS.Other;
      return`<div class="theme-book-item" data-id="${b.id}"><div class="theme-book-dot" style="background:${col}"></div><span class="theme-book-title">${esc(b.title)}</span><span class="theme-book-cat">${esc(b.category)}</span></div>`;
    }).join('')}`;
    tb.querySelectorAll(".theme-book-item").forEach(item=>item.onclick=()=>openDetail(item.dataset.id));
  });
});
}

function drawPie(cd,total){const c=document.getElementById("pieChart");if(!c)return;const dpr=window.devicePixelRatio||1;c.width=170*dpr;c.height=170*dpr;const ctx=c.getContext("2d");ctx.scale(dpr,dpr);let start=-Math.PI/2;cd.forEach(d=>{const sw=(d.value/total)*Math.PI*2;ctx.beginPath();ctx.arc(85,85,75,start,start+sw);ctx.arc(85,85,42,start+sw,start,true);ctx.closePath();ctx.fillStyle=CAT_COLORS[d.name]||"#7A7670";ctx.fill();start+=sw})}

// ══════ YEAR IN REVIEW ══════
function renderReview(){const comp=getCompleted();if(comp.length<3){document.getElementById("viewReview").innerHTML=`<div class="empty"><p class="title">Complete at least 3 books to unlock Year in Review.</p></div>`;return}
const totalPages=comp.reduce((s,b)=>s+(b.pages||0),0),pct=Math.min(100,Math.round(comp.length/getGoal()*100));const catCounts={};comp.forEach(b=>{catCounts[b.category]=(catCounts[b.category]||0)+1});const topCat=Object.entries(catCounts).sort((a,b)=>b[1]-a[1])[0];const rated=comp.filter(b=>b.rating);const avgR=rated.length?(rated.reduce((s,b)=>s+b.rating,0)/rated.length).toFixed(1):null;
const durs=comp.filter(b=>b.startDate&&b.endDate).map(b=>({...b,days:daysBetween(b.startDate,b.endDate)}));const fastest=durs.length?durs.reduce((a,b)=>a.days<b.days?a:b):null;const longest=comp.reduce((a,b)=>(b.pages||0)>(a.pages||0)?b:a,comp[0]);const topRated=[...comp].filter(b=>b.rating).sort((a,b)=>b.rating-a.rating)[0];
const allThemes={};data.books.forEach(b=>(b.themes||[]).forEach(t=>{allThemes[t]=(allThemes[t]||0)+1}));const topThemes=Object.entries(allThemes).sort((a,b)=>b[1]-a[1]).slice(0,5);
const totalQuotes=data.books.reduce((s,b)=>s+(b.quotes?.length||0),0);const ms=getMilestones();
let html=`<div class="yr-hero"><div class="yr-hero-glow"></div><p class="yr-hero-label">Year in Review</p><p class="yr-hero-num">${comp.length}</p><p class="yr-hero-sub">books completed \u00B7 ${pct}% of goal</p>${totalPages?`<p class="yr-hero-pages">${totalPages.toLocaleString()} pages read</p>`:""}</div><div class="yr-grid">`;
if(topCat)html+=`<div class="yr-card"><p class="yr-card-label">Top Category</p><p class="yr-card-value">${topCat[0]}</p><p class="yr-card-sub">${topCat[1]} books</p></div>`;if(avgR)html+=`<div class="yr-card"><p class="yr-card-label">Avg Rating</p><p class="yr-card-value">${avgR} / 5</p></div>`;if(fastest)html+=`<div class="yr-card"><p class="yr-card-label">Fastest Read</p><p class="yr-card-value sm">${esc(fastest.title)}</p><p class="yr-card-sub">${fastest.days} days</p></div>`;if(longest&&longest.pages)html+=`<div class="yr-card"><p class="yr-card-label">Longest Book</p><p class="yr-card-value sm">${esc(longest.title)}</p><p class="yr-card-sub">${longest.pages} pages</p></div>`;if(topRated)html+=`<div class="yr-card"><p class="yr-card-label">Highest Rated</p><p class="yr-card-value sm">${esc(topRated.title)}</p><p class="yr-card-sub">${topRated.rating}/5</p></div>`;html+=`</div>`;
if(topThemes.length)html+=`<div class="panel"><div class="panel-title">Recurring Themes</div><div class="yr-themes">${topThemes.map(([t,c])=>`<div class="yr-theme">${esc(t)} <em>\u00D7${c}</em></div>`).join("")}</div></div>`;
if(ms.length)html+=`<div class="panel"><div class="panel-title">Milestones</div><div class="yr-milestones">${ms.map(m=>`<div class="yr-milestone"><span class="yr-milestone-icon">${m.icon}</span><p class="yr-milestone-label">${m.label}</p></div>`).join("")}</div></div>`;
if(totalQuotes)html+=`<div style="background:var(--surface);border:1px solid var(--border);border-radius:12px;padding:18px 24px;display:flex;align-items:center;gap:14px"><span style="color:var(--accentDim);font-size:20px">\u201C</span><p style="font-family:var(--ui);font-size:13px;color:var(--textM);margin:0">You saved <strong style="color:var(--text)">${totalQuotes}</strong> passage${totalQuotes!==1?"s":""} across your readings.</p></div>`;
document.getElementById("viewReview").innerHTML=html;
}
