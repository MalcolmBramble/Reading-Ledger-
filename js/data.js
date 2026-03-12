/* ═══ DATA — Constants, utilities, data model, demo data ═══ */
/* ═══ THE READING LEDGER — Complete Build ═══ */
const CATEGORIES=["Self-Awareness","Current America","Economics & Money","Technology","American History","Science","World History","Philosophy & Ethics","Religion","Fiction","Other"];
const CAT_COLORS={"Self-Awareness":"#C4956A","Current America":"#7D8B6E","Economics & Money":"#3D5A80","Technology":"#C46B5B","American History":"#C45B72","Science":"#8B6AAC","World History":"#5B9EAD","Philosophy & Ethics":"#6EC4A7","Religion":"#9B7EC4","Fiction":"#BBA14F","Other":"#7A7670"};
const CAT_SPINE={"Self-Awareness":"#8B6540","Current America":"#556B4A","Economics & Money":"#2E4460","Technology":"#8B4B3B","American History":"#8B3B4E","Science":"#5E4570","World History":"#3B7080","Philosophy & Ethics":"#408060","Religion":"#6B5090","Fiction":"#8B7530","Other":"#5A5650"};
const STATUS_LABELS={"want-to-read":"To Read","reading":"Reading","completed":"Completed","abandoned":"Dropped"};
const SK="reading-ledger-v1";
let data,currentTab="shelf",currentSort="date",searchOpen=false,searchQuery="",confirmCb=null,detailBook=null,randomQuote=null,pendingImport=null;
let timerState={running:false,paused:false,startTime:null,elapsed:0,intervalId:null,bookId:null,_pauseAccum:0};
let pendingSessionDur=0;
window._focusIdx=0;window._tlStreamCount=20;

function uid(){return Date.now().toString(36)+Math.random().toString(36).slice(2,8)}
function esc(s){const d=document.createElement("div");d.textContent=s||"";return d.innerHTML}
function fmtDate(iso){if(!iso)return"";return new Date(iso).toLocaleDateString("en-US",{month:"short",day:"numeric",year:"numeric"})}
function fmtShort(iso){if(!iso)return"";return new Date(iso).toLocaleDateString("en-US",{month:"short",day:"numeric"})}
function daysBetween(a,b){if(!a||!b)return null;return Math.max(1,Math.round((new Date(b)-new Date(a))/86400000))}
function getGoal(){return(data.settings.goals&&data.settings.goals.annual)||data.settings.goal||50}
function getCompleted(){return data.books.filter(b=>b.status==="completed")}
function getReading(){return data.books.filter(b=>b.status==="reading")}
function load(){try{const r=localStorage.getItem(SK);if(r){const d=JSON.parse(r);if(d.books&&d.books.length>0)return d}}catch(e){}return null}
function save(){localStorage.setItem(SK,JSON.stringify(data))}

function getDayStreak(){
  const sessionDays={};data.books.forEach(b=>(b.sessions||[]).forEach(s=>{if(!s.date)return;const k=s.date.slice(0,10);sessionDays[k]=(sessionDays[k]||0)+1}));
  const today=new Date();today.setHours(0,0,0,0);
  let current=0;for(let d=new Date(today);;d.setDate(d.getDate()-1)){if(sessionDays[d.toISOString().slice(0,10)])current++;else break}
  let best=0,run=0;const keys=Object.keys(sessionDays).sort();keys.forEach((k,i)=>{if(i===0)run=1;else{const prev=new Date(keys[i-1]);prev.setDate(prev.getDate()+1);run=prev.toISOString().slice(0,10)===k?run+1:1}if(run>best)best=run});
  // This week (Mon-Sun)
  const dow=today.getDay()===0?6:today.getDay()-1;const weekStart=new Date(today);weekStart.setDate(weekStart.getDate()-dow);let weekRead=0;
  for(let i=0;i<7;i++){const wd=new Date(weekStart);wd.setDate(wd.getDate()+i);if(sessionDays[wd.toISOString().slice(0,10)])weekRead++}
  // This month
  const cm=today.getMonth(),cy=today.getFullYear(),dim=new Date(cy,cm+1,0).getDate();let monthRead=0;
  for(let d=1;d<=dim;d++){const dk=`${cy}-${String(cm+1).padStart(2,'0')}-${String(d).padStart(2,'0')}`;if(sessionDays[dk])monthRead++}
  return{current,best,weekRead,monthRead,monthDays:dim,sessionDays};
}

// ═══ AUTO-MILESTONE ENGINE (50 rules) ═══
// Returns {earned:[], next:{}} — earned milestones + next upcoming
function autoMilestones(){
const comp=getCompleted().sort((a,b)=>new Date(a.endDate)-new Date(b.endDate));
const all=data.books;const ab=all.filter(b=>b.status==="abandoned");const reading=getReading();
if(!comp.length)return{earned:[],next:{icon:"book",color:"var(--accent)",label:"Finish Your First Book",detail:"Start reading!",pct:0}};
const totalPages=comp.reduce((s,b)=>s+(b.pages||0),0);
const cats=new Set(comp.map(b=>b.category));
const durs=comp.filter(b=>b.startDate&&b.endDate).map(b=>({...b,days:Math.max(1,Math.round((new Date(b.endDate)-new Date(b.startDate))/86400000))}));
const ratings=comp.filter(b=>b.rating>0);
const annotated=all.filter(b=>b.notes||b.coreArgument||b.impact||(b.quotes&&b.quotes.length)).length;
const totalQuotes=all.reduce((s,b)=>s+(b.quotes||[]).length,0);
const monthMap={};comp.forEach(b=>{if(b.endDate){const k=b.endDate.slice(0,7);monthMap[k]=(monthMap[k]||0)+1}});
const monthNames={"01":"Jan","02":"Feb","03":"Mar","04":"Apr","05":"May","06":"Jun","07":"Jul","08":"Aug","09":"Sep","10":"Oct","11":"Nov","12":"Dec"};
const allSessions=[];all.forEach(b=>(b.sessions||[]).forEach(s=>allSessions.push({...s,book:b})));
const earned=[];

// ── COUNT MILESTONES (1-9) ──
if(comp.length>=1)earned.push({icon:"book",color:"var(--accent)",label:"First Book",detail:comp[0].title});
[5,10,15,20,25,30,40,50].forEach(n=>{if(comp.length>=n)earned.push({icon:n>=25?"star":"books",color:n>=25?"var(--gold)":"var(--accent)",label:`${n} Books`,detail:`Reached with ${comp[n-1].title}`})});

// ── PAGE MILESTONES (10-12) ──
if(totalPages>=10000)earned.push({icon:"ruler",color:"#8B6AAC",label:"10,000 Pages",detail:`${totalPages.toLocaleString()} total`});
else if(totalPages>=5000)earned.push({icon:"ruler",color:"#8B6AAC",label:"5,000 Pages",detail:`${totalPages.toLocaleString()} total`});
else if(totalPages>=2000)earned.push({icon:"ruler",color:"#8B6AAC",label:"2,000 Pages",detail:`${totalPages.toLocaleString()} total`});

// ── RATING MILESTONES (13-16) ──
const fiveStars=ratings.filter(b=>b.rating===5);
if(fiveStars.length>=1)earned.push({icon:"star",color:"var(--gold)",label:"First 5-Star",detail:fiveStars[0].title});
if(fiveStars.length>=5)earned.push({icon:"star",color:"var(--gold)",label:"5 Five-Star Books",detail:"Discerning taste"});
if(ratings.length>=5&&ratings.every(b=>b.rating>=4))earned.push({icon:"star",color:"var(--gold)",label:"No Book Below 4★",detail:"High standards"});
if(ratings.length>=3&&ratings.every(b=>b.rating===5))earned.push({icon:"star",color:"var(--gold)",label:"Perfect Streak",detail:"All 5 stars"});

// ── PACE MILESTONES (17-22) ──
if(durs.length){
  const fastest=durs.reduce((a,b)=>a.days<b.days?a:b);
  if(fastest.days<=3)earned.push({icon:"bolt",color:"#C46B5B",label:"Lightning Read",detail:`${fastest.title} in ${fastest.days}d`});
  else if(fastest.days<=7)earned.push({icon:"bolt",color:"#C46B5B",label:"Speed Reader",detail:`${fastest.title} in ${fastest.days}d`});
  else if(fastest.days<=14)earned.push({icon:"bolt",color:"#C46B5B",label:"Quick Finish",detail:`${fastest.title} in ${fastest.days}d`});
  const slowest=durs.reduce((a,b)=>a.days>b.days?a:b);
  if(slowest.days>=90)earned.push({icon:"clock",color:"var(--textD)",label:"The Long Haul",detail:`${slowest.title} — ${slowest.days} days`});
}
const biggest=comp.reduce((a,b)=>(a.pages||0)>(b.pages||0)?a:b,comp[0]);
if(biggest.pages>=1000)earned.push({icon:"brick",color:"var(--blue)",label:"Thousand-Pager",detail:`${biggest.title} — ${biggest.pages.toLocaleString()}p`});
else if(biggest.pages>=500)earned.push({icon:"brick",color:"var(--blue)",label:"Marathon Read",detail:`${biggest.title} — ${biggest.pages}p`});

// ── CATEGORY MILESTONES (23-28) ──
if(cats.size>=8)earned.push({icon:"rainbow",color:"var(--green)",label:"Renaissance Reader",detail:`${cats.size} categories`});
else if(cats.size>=5)earned.push({icon:"rainbow",color:"var(--green)",label:`${cats.size} Categories`,detail:"Wide-ranging taste"});
if(cats.size===1&&comp.length>=3)earned.push({icon:"target",color:"var(--accent)",label:"Specialist",detail:`All ${[...cats][0]}`});
const catCounts={};comp.forEach(b=>catCounts[b.category]=(catCounts[b.category]||0)+1);
const topCatEntry=Object.entries(catCounts).sort((a,b)=>b[1]-a[1])[0];
if(topCatEntry&&topCatEntry[1]>=5)earned.push({icon:"target",color:"var(--accent)",label:`${topCatEntry[0]} Expert`,detail:`${topCatEntry[1]} books`});
if(comp.length>=3){let maxS=1,curS=1,sCat=comp[0].category;for(let i=1;i<comp.length;i++){if(comp[i].category===comp[i-1].category){curS++;if(curS>maxS){maxS=curS;sCat=comp[i].category}}else curS=1}if(maxS>=3)earned.push({icon:"target",color:"var(--accent)",label:`${sCat} Deep Dive`,detail:`${maxS} in a row`})}

// ── MONTH/TEMPORAL MILESTONES (29-34) ──
const bestMonth=Object.entries(monthMap).sort((a,b)=>b[1]-a[1])[0];
if(bestMonth&&bestMonth[1]>=3)earned.push({icon:"flame",color:"#C45B5B",label:`${bestMonth[1]} in One Month`,detail:monthNames[bestMonth[0].split("-")[1]]||bestMonth[0]});
else if(bestMonth&&bestMonth[1]>=2)earned.push({icon:"flame",color:"#C45B5B",label:"Double Month",detail:monthNames[bestMonth[0].split("-")[1]]||bestMonth[0]});
const activeMonths=Object.keys(monthMap).length;
if(activeMonths>=10)earned.push({icon:"calendar",color:"var(--green)",label:"Year-Round Reader",detail:`${activeMonths} months active`});
else if(activeMonths>=6)earned.push({icon:"calendar",color:"var(--green)",label:"Steady Habit",detail:`${activeMonths} months active`});
for(let i=1;i<comp.length;i++){const gap=Math.round((new Date(comp[i].endDate)-new Date(comp[i-1].endDate))/86400000);if(gap<=3){earned.push({icon:"bolt",color:"#C46B5B",label:"Back to Back",detail:`${comp[i-1].title} → ${comp[i].title}`});break}}

// ── CONTENT MILESTONES (35-42) ──
if(annotated>=10)earned.push({icon:"pencil",color:"var(--accent)",label:"Deep Annotator",detail:`${annotated} books with notes`});
else if(annotated>=5)earned.push({icon:"pencil",color:"var(--accent)",label:"Active Reader",detail:`${annotated} books annotated`});
if(totalQuotes>=20)earned.push({icon:"quote",color:"var(--accentDim)",label:"Quote Collector",detail:`${totalQuotes} passages saved`});
else if(totalQuotes>=10)earned.push({icon:"quote",color:"var(--accentDim)",label:"Passage Hunter",detail:`${totalQuotes} saved`});
else if(totalQuotes>=5)earned.push({icon:"quote",color:"var(--accentDim)",label:"First Passages",detail:`${totalQuotes} saved`});
const allThemes={};all.forEach(b=>(b.themes||[]).forEach(t=>{const k=t.toLowerCase();allThemes[k]=(allThemes[k]||0)+1}));
if(Object.keys(allThemes).length>=20)earned.push({icon:"grid",color:"#5B9EAD",label:"Theme Mapper",detail:`${Object.keys(allThemes).length} unique themes`});
const themeMax=Object.entries(allThemes).sort((a,b)=>b[1]-a[1])[0];
if(themeMax&&themeMax[1]>=4)earned.push({icon:"grid",color:"#5B9EAD",label:`"${themeMax[0]}" Thread`,detail:`Appears in ${themeMax[1]} books`});
if(ab.length===0&&comp.length>=5)earned.push({icon:"check",color:"var(--green)",label:"Completionist",detail:"Zero abandoned books"});

// ── BEHAVIORAL MILESTONES (43-47) ──
if(allSessions.length>=50)earned.push({icon:"clock",color:"var(--blue)",label:"50 Sessions",detail:"Dedicated tracker"});
else if(allSessions.length>=20)earned.push({icon:"clock",color:"var(--blue)",label:"20 Sessions",detail:"Building the habit"});
const totalMin=allSessions.reduce((s,x)=>s+(x.duration||0),0);
if(totalMin>=1200)earned.push({icon:"clock",color:"var(--blue)",label:"20+ Hours Reading",detail:`${Math.round(totalMin/60)}h tracked`});
if(reading.length>=3)earned.push({icon:"books",color:"var(--accent)",label:"Parallel Reader",detail:`${reading.length} books at once`});
const recs=all.filter(b=>b.recommendedBy);
if(recs.length>=3)earned.push({icon:"heart",color:"#C45B72",label:"Trust Your People",detail:`${recs.length} recommended books`});

// ── IDENTITY MILESTONES (48-50) ──
if(comp.length>=4){const fc=comp[0].category,lc=comp[comp.length-1].category;if(fc!==lc)earned.push({icon:"compass",color:"#6EC4A7",label:"Evolving Taste",detail:`${fc} → ${lc}`})}
const fiction=comp.filter(b=>b.category==="Fiction").length;
if(fiction===0&&comp.length>=5)earned.push({icon:"book",color:"var(--accent)",label:"All Nonfiction",detail:"Not a single novel"});
if(comp.length>=10&&cats.size>=5){const balanced=Object.values(catCounts).every(c=>c<=comp.length*0.3);if(balanced)earned.push({icon:"rainbow",color:"var(--green)",label:"Balanced Reader",detail:"No category over 30%"})}

// ── NEXT MILESTONE ──
let next=null;
const nextCount=[5,10,15,20,25,30,40,50].find(n=>n>comp.length);
if(nextCount){const rem=nextCount-comp.length;next={icon:"target",color:"var(--textD)",label:`${rem} to ${nextCount} Books`,detail:`${comp.length} of ${nextCount}`,pct:Math.round(comp.length/nextCount*100)}}
else if(totalPages<5000)next={icon:"ruler",color:"var(--textD)",label:`${(5000-totalPages).toLocaleString()} to 5,000 Pages`,detail:`${totalPages.toLocaleString()} so far`,pct:Math.round(totalPages/5000*100)};

return{earned,next};
}

// Backward compat wrapper for shelf banner
function getMilestones(){return autoMilestones().earned.slice(-4).map(m=>({label:m.label,icon:""}))}

function getCategoryProgress(cat){const target=(data.settings.goals.categories||{})[cat]||0;if(!target)return null;const count=getCompleted().filter(b=>b.category===cat).length;return{target,count,pct:Math.min(100,Math.round(count/target*100))}}
function getChallengeProgress(ch){const completed=getCompleted();let current=0;if(ch.type==="count"){let pool=completed;if(ch.categoryFilter)pool=pool.filter(b=>b.category===ch.categoryFilter);if(ch.minPages)pool=pool.filter(b=>(b.pages||0)>=ch.minPages);current=pool.length}else if(ch.type==="pages"){let pool=completed;if(ch.categoryFilter)pool=pool.filter(b=>b.category===ch.categoryFilter);current=pool.reduce((s,b)=>s+(b.pages||0),0)}else if(ch.type==="category"){current=completed.filter(b=>b.category===ch.categoryFilter).length}else if(ch.type==="diversity"){current=new Set(completed.map(b=>b.category)).size}return{current,target:ch.target,pct:Math.min(100,Math.round(current/ch.target*100)),done:current>=ch.target}}

function exportCSV(){
  const headers=["Title","Author","Category","Status","Rating","Pages","Start Date","End Date","Themes","Notes"];
  const rows=data.books.map(b=>[b.title||"",b.author||"",b.category||"",b.status||"",b.rating||"",b.pages||"",b.startDate||"",b.endDate||"",(b.themes||[]).join("; "),(b.notes||"").slice(0,200)]);
  const csvEsc=v=>{const s=String(v);return s.includes(",")||s.includes('"')||s.includes("\n")?'"'+s.replace(/"/g,'""')+'"':s};
  const csv=[headers.map(csvEsc).join(","),...rows.map(r=>r.map(csvEsc).join(","))].join("\n");
  const blob=new Blob([csv],{type:"text/csv"});const a=document.createElement("a");a.href=URL.createObjectURL(blob);a.download=`reading-ledger-${new Date().toISOString().slice(0,10)}.csv`;a.click();URL.revokeObjectURL(a.href);data.settings.lastExport=new Date().toISOString();save();
}

function pickRandomQuote(){const qs=[];data.books.forEach(b=>(b.quotes||[]).forEach(q=>qs.push({text:q.text,page:q.page,bookTitle:b.title,bookAuthor:b.author})));randomQuote=qs.length?qs[Math.floor(Math.random()*qs.length)]:null}

// ─── Demo Data ───
function makeDemoData(){
const now=new Date().toISOString();
const books=[
{id:uid(),title:"Atomic Habits",author:"James Clear",category:"Self-Awareness",status:"completed",pages:320,currentPage:320,rating:5,startDate:"2025-01-05",endDate:"2025-01-18",notes:"Remarkable framework for behavior change.",themes:["habits","systems","identity"],quotes:[{id:uid(),text:"You do not rise to the level of your goals. You fall to the level of your systems.",page:"27",addedAt:now},{id:uid(),text:"Every action you take is a vote for the type of person you wish to become.",page:"38",addedAt:now}],connections:[],sessions:[{id:uid(),date:"2025-01-08",startPage:0,endPage:100,duration:60,notes:""},{id:uid(),date:"2025-01-12",startPage:100,endPage:220,duration:75,notes:""},{id:uid(),date:"2025-01-17",startPage:220,endPage:320,duration:55,notes:""}],coreArgument:"Small habits compound into remarkable results over time.",impact:"Changed how I think about daily routines.",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2025-01-05T10:00:00Z",updatedAt:now},
{id:uid(),title:"Thinking, Fast and Slow",author:"Daniel Kahneman",category:"Science",status:"completed",pages:499,currentPage:499,rating:5,startDate:"2025-02-01",endDate:"2025-03-02",notes:"Dense but essential on cognitive biases.",themes:["cognition","bias","decision-making"],quotes:[{id:uid(),text:"Nothing in life is as important as you think it is, while you are thinking about it.",page:"402",addedAt:now}],connections:[],sessions:[{id:uid(),date:"2025-02-10",startPage:0,endPage:180,duration:120,notes:""},{id:uid(),date:"2025-02-20",startPage:180,endPage:350,duration:90,notes:""},{id:uid(),date:"2025-03-01",startPage:350,endPage:499,duration:80,notes:""}],coreArgument:"Two systems govern thought: fast intuition and slow deliberation.",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2025-02-01T10:00:00Z",updatedAt:now},
{id:uid(),title:"The Wealth of Nations",author:"Adam Smith",category:"Economics & Money",status:"completed",pages:1264,currentPage:1264,rating:4,startDate:"2024-11-01",endDate:"2025-01-15",notes:"Foundational text of modern economics.",themes:["capitalism","markets","labor"],quotes:[],connections:[],sessions:[],coreArgument:"Free markets guided by self-interest produce collective prosperity.",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2024-11-01T10:00:00Z",updatedAt:now},
{id:uid(),title:"Sapiens",author:"Yuval Noah Harari",category:"World History",status:"completed",pages:443,currentPage:443,rating:4,startDate:"2025-03-10",endDate:"2025-04-05",notes:"Sweeping narrative of human history.",themes:["civilization","evolution","narrative"],quotes:[{id:uid(),text:"The real difference between us and chimpanzees is the mythical glue that binds together large numbers of individuals.",page:"25",addedAt:now}],connections:[],sessions:[{id:uid(),date:"2025-03-15",startPage:0,endPage:150,duration:90,notes:""},{id:uid(),date:"2025-03-25",startPage:150,endPage:320,duration:100,notes:""},{id:uid(),date:"2025-04-04",startPage:320,endPage:443,duration:70,notes:""}],coreArgument:"Shared myths enable human cooperation at scale.",impact:"Reframed how I understand institutions.",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2025-03-10T10:00:00Z",updatedAt:now},
{id:uid(),title:"Meditations",author:"Marcus Aurelius",category:"Philosophy & Ethics",status:"completed",pages:256,currentPage:256,rating:5,startDate:"2025-04-10",endDate:"2025-04-20",notes:"Timeless. Every page has something to sit with.",themes:["stoicism","virtue","mortality"],quotes:[{id:uid(),text:"The happiness of your life depends upon the quality of your thoughts.",page:"",addedAt:now}],connections:[],sessions:[{id:uid(),date:"2025-04-12",startPage:0,endPage:128,duration:45,notes:""},{id:uid(),date:"2025-04-19",startPage:128,endPage:256,duration:50,notes:""}],coreArgument:"Inner peace through acceptance of what you cannot control.",impact:"Daily reference now.",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2025-04-10T10:00:00Z",updatedAt:now},
{id:uid(),title:"1984",author:"George Orwell",category:"Fiction",status:"completed",pages:328,currentPage:328,rating:4,startDate:"2025-05-01",endDate:"2025-05-12",notes:"Chillingly relevant.",themes:["totalitarianism","surveillance","language"],quotes:[],connections:[],sessions:[{id:uid(),date:"2025-05-05",startPage:0,endPage:160,duration:80,notes:""},{id:uid(),date:"2025-05-11",startPage:160,endPage:328,duration:85,notes:""}],coreArgument:"",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2025-05-01T10:00:00Z",updatedAt:now},
{id:uid(),title:"The Gene",author:"Siddhartha Mukherjee",category:"Science",status:"reading",pages:592,currentPage:340,rating:0,startDate:"2026-02-15",endDate:"",notes:"Beautifully written history of genetics.",themes:["genetics","science","ethics"],quotes:[],connections:[],sessions:[{id:uid(),date:"2026-02-20",startPage:0,endPage:120,duration:90,notes:""},{id:uid(),date:"2026-02-25",startPage:120,endPage:240,duration:75,notes:""},{id:uid(),date:"2026-03-05",startPage:240,endPage:340,duration:60,notes:""}],coreArgument:"",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2026-02-15T10:00:00Z",updatedAt:now},
{id:uid(),title:"Democracy in America",author:"Alexis de Tocqueville",category:"American History",status:"reading",pages:864,currentPage:210,rating:0,startDate:"2026-03-01",endDate:"",notes:"",themes:["democracy","America","institutions"],quotes:[],connections:[],sessions:[{id:uid(),date:"2026-03-05",startPage:0,endPage:100,duration:120,notes:""},{id:uid(),date:"2026-03-09",startPage:100,endPage:210,duration:80,notes:""}],coreArgument:"",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2026-03-01T10:00:00Z",updatedAt:now},
{id:uid(),title:"The Innovators",author:"Walter Isaacson",category:"Technology",status:"want-to-read",pages:542,currentPage:0,rating:0,startDate:"",endDate:"",notes:"",themes:[],quotes:[],connections:[],sessions:[],coreArgument:"",impact:"",recommendedBy:"Sam",recommendationNote:"Essential tech history",recommendationSource:"conversation",priority:2,addedAt:"2026-01-15T10:00:00Z",updatedAt:now},
{id:uid(),title:"The Righteous Mind",author:"Jonathan Haidt",category:"Current America",status:"want-to-read",pages:419,currentPage:0,rating:0,startDate:"",endDate:"",notes:"",themes:[],quotes:[],connections:[],sessions:[],coreArgument:"",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2026-02-01T10:00:00Z",updatedAt:now},
{id:uid(),title:"The Brothers Karamazov",author:"Fyodor Dostoevsky",category:"Fiction",status:"want-to-read",pages:796,currentPage:0,rating:0,startDate:"",endDate:"",notes:"",themes:[],quotes:[],connections:[],sessions:[],coreArgument:"",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2026-03-01T10:00:00Z",updatedAt:now},
{id:uid(),title:"Being and Time",author:"Martin Heidegger",category:"Philosophy & Ethics",status:"abandoned",pages:589,currentPage:95,rating:2,startDate:"2025-06-01",endDate:"2025-06-20",notes:"Too dense. Will revisit.",themes:["existentialism"],quotes:[],connections:[],sessions:[],coreArgument:"",impact:"",recommendedBy:"",recommendationNote:"",recommendationSource:"",priority:0,addedAt:"2025-06-01T10:00:00Z",updatedAt:now}
];
return{books,settings:{goal:50,lastExport:null,goals:{annual:50,categories:{},challenges:[]}}};
}

data=load()||makeDemoData();
if(!data.settings.goals)data.settings.goals={annual:data.settings.goal||50,categories:{},challenges:[]};
if(!data.settings.goals.challenges)data.settings.goals.challenges=[];
data.books.forEach(b=>{if(!b.sessions)b.sessions=[];if(!b.themes)b.themes=[];if(!b.quotes)b.quotes=[];if(!b.connections)b.connections=[];if(b.priority===undefined)b.priority=0;if(!b.recommendedBy)b.recommendedBy="";if(!b.recommendationNote)b.recommendationNote="";if(!b.recommendationSource)b.recommendationSource=""});
save();pickRandomQuote();

// ─── Toast ───
