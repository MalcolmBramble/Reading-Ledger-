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
// ═══ SVG icons for milestones ═══
const MS_ICONS={book:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 016.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 014 19.5v-15A2.5 2.5 0 016.5 2z"/></svg>',star:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/></svg>',brick:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><line x1="3" y1="9" x2="21" y2="9"/><line x1="3" y1="15" x2="21" y2="15"/><line x1="9" y1="3" x2="9" y2="9"/><line x1="15" y1="9" x2="15" y2="15"/><line x1="9" y1="15" x2="9" y2="21"/></svg>',bolt:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2"/></svg>',rainbow:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M22 17a10 10 0 00-20 0"/><path d="M19 17a7 7 0 00-14 0"/><path d="M16 17a4 4 0 00-8 0"/></svg>',flame:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M8.5 14.5A2.5 2.5 0 0011 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 11-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 002.5 2.5z"/></svg>',ruler:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M21.3 15.3a2.4 2.4 0 010 3.4l-2.6 2.6a2.4 2.4 0 01-3.4 0L2.7 8.7a2.4 2.4 0 010-3.4l2.6-2.6a2.4 2.4 0 013.4 0z"/><line x1="14.5" y1="12.5" x2="16.5" y2="10.5"/><line x1="10.5" y1="16.5" x2="12.5" y2="14.5"/></svg>',target:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="2"/></svg>',openbook:'<svg viewBox="0 0 24 24" stroke="currentColor" fill="none" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M2 3h6a4 4 0 014 4v14a3 3 0 00-3-3H2z"/><path d="M22 3h-6a4 4 0 00-4 4v14a3 3 0 013-3h7z"/></svg>'};

// ═══ Auto-milestones ═══
function autoMilestones(comp,allBooks){
const ms=[];const sorted=[...comp].sort((a,b)=>new Date(a.endDate)-new Date(b.endDate));
if(sorted.length>=1)ms.push({icon:'book',color:'var(--accent)',label:'First Book',detail:sorted[0].title});
[5,10,15,20,25,30,40,50].forEach(n=>{if(sorted.length>=n)ms.push({icon:n>=25?'star':'book',color:n>=25?'var(--gold)':'var(--accent)',label:`${n} Books`,detail:`Reached with ${sorted[n-1].title}`})});
const first5=sorted.find(b=>b.rating===5);if(first5)ms.push({icon:'star',color:'var(--gold)',label:'First 5-Star',detail:first5.title});
const longest=comp.reduce((a,b)=>(b.pages||0)>(a.pages||0)?b:a,comp[0]);if(longest.pages>=400)ms.push({icon:'brick',color:'var(--blue)',label:'Marathon Read',detail:`${longest.title} \u2014 ${longest.pages.toLocaleString()} pages`});
const withDays=comp.filter(b=>b.startDate&&b.endDate).map(b=>({...b,days:daysBetween(b.startDate,b.endDate)}));const fastest=withDays.length?withDays.reduce((a,b)=>a.days<b.days?a:b):null;
if(fastest&&fastest.days<=14)ms.push({icon:'bolt',color:'#C46B5B',label:'Speed Reader',detail:`${fastest.title} in ${fastest.days} days`});
const cats=new Set(comp.map(b=>b.category));if(cats.size>=5)ms.push({icon:'rainbow',color:'var(--green)',label:`${cats.size} Categories`,detail:'Renaissance reader'});
const mc={};sorted.forEach(b=>{if(b.endDate){const k=b.endDate.slice(0,7);mc[k]=(mc[k]||0)+1}});const bestMonth=Object.entries(mc).sort((a,b)=>b[1]-a[1])[0];
if(bestMonth&&bestMonth[1]>=2){const[y,m]=bestMonth[0].split('-');ms.push({icon:'flame',color:'#C45B5B',label:`${bestMonth[1]} in One Month`,detail:new Date(+y,+m-1).toLocaleDateString('en-US',{month:'long'})})}
const tp=comp.reduce((s,b)=>s+(b.pages||0),0);if(tp>=5000)ms.push({icon:'ruler',color:'#8B6AAC',label:'5,000 Pages',detail:`${tp.toLocaleString()} total`});else if(tp>=2000)ms.push({icon:'ruler',color:'#8B6AAC',label:'2,000 Pages',detail:`${tp.toLocaleString()} total`});
const nextTarget=[5,10,15,20,25,30,40,50].find(n=>n>sorted.length);
if(nextTarget){const rem=nextTarget-sorted.length;ms.push({icon:'target',color:'var(--textD)',label:`${rem} to ${nextTarget} Books`,detail:`${sorted.length} of ${nextTarget}`,next:true,pct:Math.round(sorted.length/nextTarget*100)})}
return ms;
}

// ═══ Reader identity ═══
const ID_LABELS={"Self-Awareness":"Seeker","Current America":"Citizen","Economics & Money":"Economist","Technology":"Technologist","American History":"Historian","Science":"Scientist","World History":"Historian","Philosophy & Ethics":"Philosopher","Religion":"Contemplative","Fiction":"Storyteller","Other":"Explorer"};
function getReaderIdentity(slice){
  const cc={};slice.forEach(b=>cc[b.category]=(cc[b.category]||0)+1);const s=Object.entries(cc).sort((a,b)=>b[1]-a[1]);
  const w1=ID_LABELS[s[0][0]]||"Reader",w2=s[1]?ID_LABELS[s[1][0]]||"Reader":null;
  return w2&&w2!==w1?`The ${w1}-${w2}`:`The ${w1}`;
}
function getIdentityEvolution(comp){
  const steps=[];const checks=[2,4,comp.length].filter((v,i,a)=>v<=comp.length&&a.indexOf(v)===i);
  const sorted=[...comp].sort((a,b)=>new Date(a.endDate)-new Date(b.endDate));
  checks.forEach(n=>{const id=getReaderIdentity(sorted.slice(0,n));const last=sorted[n-1];
    const period=n===comp.length?'Now':fmtShort(sorted[0].endDate)+' \u2013 '+fmtShort(last.endDate);
    if(!steps.length||steps[steps.length-1].id!==id)steps.push({id,period,current:n===comp.length})});
  return steps;
}

// ══════ YEAR IN REVIEW ══════
function renderReview(){
const comp=getCompleted();if(comp.length<3){document.getElementById("viewReview").innerHTML=`<div class="empty"><p class="title">Complete at least 3 books to unlock Year in Review.</p></div>`;return}
const totalPages=comp.reduce((s,b)=>s+(b.pages||0),0),pct=Math.min(100,Math.round(comp.length/getGoal()*100));
const rated=comp.filter(b=>b.rating),avgR=rated.length?(rated.reduce((s,b)=>s+b.rating,0)/rated.length).toFixed(1):null;
const ratingCounts=[0,0,0,0,0];comp.forEach(b=>{if(b.rating>=1&&b.rating<=5)ratingCounts[b.rating-1]++});const maxRat=Math.max(...ratingCounts,1);
const cats=new Set(comp.map(b=>b.category)),avgPages=Math.round(totalPages/comp.length);
const fiveCount=ratingCounts[4];
let html='';

// 1. HERO
html+=`<div class="yr-hero"><div class="yr-hero-glow"></div><p class="yr-hero-label">Year in Review</p><p class="yr-hero-num">${comp.length}</p><p class="yr-hero-sub">books completed \u00B7 ${pct}% of goal</p><p class="yr-hero-pages">${totalPages.toLocaleString()} pages read</p></div>`;

// 2. MONTHLY TIMELINE
const monthNames=['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
const monthBooks={};comp.forEach(b=>{if(b.endDate){const d=new Date(b.endDate);const k=d.getFullYear()+'-'+String(d.getMonth()).padStart(2,'0');if(!monthBooks[k])monthBooks[k]=[];monthBooks[k].push(b)}});
html+=`<div class="yr-months">`;
for(let m=0;m<12;m++){const yr=new Date().getFullYear();const k=yr+'-'+String(m).padStart(2,'0');const k2=(yr-1)+'-'+String(m).padStart(2,'0');const mb=monthBooks[k]||monthBooks[k2]||[];
html+=`<div class="yr-month"><div class="yr-month-dots">`;if(mb.length)mb.forEach(b=>{const col=CAT_COLORS[b.category]||CAT_COLORS.Other;html+=`<div class="yr-month-dot" style="background:${col}"></div>`});else html+=`<div class="yr-month-empty"></div>`;
html+=`</div><span class="yr-month-label">${monthNames[m]}</span></div>`}
html+=`</div>`;

// 3. NARRATIVE (from insight engine)
const insightText=composeInsightText(data.books);
if(insightText)html+=`<div class="panel"><div class="panel-title">Your Reading Journey</div><p class="yr-narrative">${insightText}</p></div>`;

// 4. IDENTITY CARD
const currentId=getReaderIdentity(comp);const evolution=getIdentityEvolution(comp);
html+=`<div class="yr-identity"><div class="yr-id-bg"></div><div class="yr-id-accent-line"></div><div class="yr-id-content"><div class="yr-id-header"><div class="yr-id-icon">${MS_ICONS.openbook}</div><div class="yr-id-text"><p class="yr-id-label">Your reader identity</p><h2 class="yr-id-title">${currentId}</h2><p class="yr-id-desc">${cats.size} categories \u00B7 ${avgPages} avg pages \u00B7 ${fiveCount>=comp.length/2?'high standards':'wide-ranging taste'}</p></div></div>`;
// Stats strip
html+=`<div class="yr-id-stats"><div class="yr-id-stat"><p class="yr-id-stat-val">${comp.length}</p><p class="yr-id-stat-label">Books</p></div><div class="yr-id-stat"><p class="yr-id-stat-val">${avgPages}</p><p class="yr-id-stat-label">Avg pages</p></div><div class="yr-id-stat"><p class="yr-id-stat-val">${cats.size}</p><p class="yr-id-stat-label">Categories</p></div><div class="yr-id-stat"><p class="yr-id-stat-val yr-gold">${avgR||'\u2014'}</p><p class="yr-id-stat-label">Avg rating</p></div></div>`;
// Rating bars
html+=`<div class="yr-id-ratings"><div><div class="yr-id-rat-visual">`;
for(let i=0;i<5;i++){const h=ratingCounts[i]>0?Math.max(4,Math.round(ratingCounts[i]/maxRat*18)):2;const col=i>=3?'var(--gold)':'var(--textDD)';html+=`<div class="yr-id-rat-bar" style="height:${h}px;background:${col}"></div>`}
html+=`</div><div class="yr-id-rat-labels">`;for(let i=0;i<5;i++)html+=`<span class="yr-id-rat-lbl">${i+1}</span>`;
html+=`</div></div><span class="yr-id-rat-text"><strong>${avgR||'\u2014'}</strong>/5 avg \u00B7 ${fiveCount} of ${comp.length} got 5\u2605</span></div>`;
// Evolution
if(evolution.length>1){html+=`<div class="yr-evo"><p class="yr-evo-title">How your identity shifted</p><div class="yr-evo-flow">`;
evolution.forEach((s,i)=>{html+=`<div style="text-align:center"><span class="yr-evo-chip${s.current?' yr-evo-now':''}">${s.id}</span><span class="yr-evo-period">${s.period}</span></div>`;if(i<evolution.length-1)html+=`<span class="yr-evo-arrow">\u2192</span>`});
html+=`</div></div>`}
html+=`</div></div>`;

// 5. FAVORITE PASSAGE
const allQuotes=[];data.books.forEach(b=>(b.quotes||[]).forEach(q=>allQuotes.push({text:q.text,page:q.page,title:b.title,author:b.author,rating:b.rating||0})));
const bestQuote=allQuotes.sort((a,b)=>b.rating-a.rating)[0];
if(bestQuote)html+=`<div class="yr-quote"><p class="yr-quote-text">\u201C${esc(bestQuote.text)}\u201D</p><p class="yr-quote-src">\u2014 <strong>${esc(bestQuote.title)}</strong> by ${esc(bestQuote.author)}</p></div>`;

// 6. MILESTONES
const ms=autoMilestones(comp,data.books);
html+=`<div class="panel"><div class="panel-title">Milestones</div><div class="yr-ms-list">`;
ms.forEach(m=>{const icon=MS_ICONS[m.icon]||MS_ICONS.book;
html+=`<div class="yr-ms-item${m.next?' yr-ms-next':''}"><div class="yr-ms-icon-wrap" style="background:${m.color}15;color:${m.color}">${icon}</div><div class="yr-ms-info"><p class="yr-ms-label">${m.label}</p><p class="yr-ms-detail">${m.detail}</p>${m.next?`<div class="yr-ms-bar"><div class="yr-ms-fill" style="width:${m.pct}%"></div></div>`:''}</div></div>`});
html+=`</div></div>`;

document.getElementById("viewReview").innerHTML=html;
}
