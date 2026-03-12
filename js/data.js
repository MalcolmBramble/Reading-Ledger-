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

function getMilestones(){
  const c=getCompleted(),ms=[];
  [1,5,10,15,20,25,30,40,50].forEach(t=>{if(c.length>=t)ms.push({label:t===1?"First Book":t===50?"Goal Reached!":t+" Books",icon:t===50?"\u{1F3C6}":t>=25?"\u2B50":"\u{1F4D6}"})});
  const cats=new Set(c.map(b=>b.category));
  if(cats.size>=5)ms.push({label:cats.size+" Categories",icon:"\u{1F308}"});
  if(cats.size>=8)ms.push({label:"Renaissance Reader",icon:"\u{1F3AD}"});
  const ann=data.books.filter(b=>b.notes||b.coreArgument||b.impact||(b.quotes&&b.quotes.length>0)).length;
  if(ann>=10)ms.push({label:"Deep Annotator",icon:"\u270D\uFE0F"});
  const conn=data.books.filter(b=>b.connections&&b.connections.length>0).length;
  if(conn>=5)ms.push({label:"Web Weaver",icon:"\u{1F578}\uFE0F"});
  return ms;
}

function getCategoryProgress(cat){const target=(data.settings.goals.categories||{})[cat]||0;if(!target)return null;const count=getCompleted().filter(b=>b.category===cat).length;return{target,count,pct:Math.min(100,Math.round(count/target*100))}}
function getChallengeProgress(ch){const completed=getCompleted();let current=0;if(ch.type==="count"){let pool=completed;if(ch.categoryFilter)pool=pool.filter(b=>b.category===ch.categoryFilter);if(ch.minPages)pool=pool.filter(b=>(b.pages||0)>=ch.minPages);current=pool.length}else if(ch.type==="pages"){let pool=completed;if(ch.categoryFilter)pool=pool.filter(b=>b.category===ch.categoryFilter);current=pool.reduce((s,b)=>s+(b.pages||0),0)}else if(ch.type==="category"){current=completed.filter(b=>b.category===ch.categoryFilter).length}else if(ch.type==="diversity"){current=new Set(completed.map(b=>b.category)).size}return{current,target:ch.target,pct:Math.min(100,Math.round(current/ch.target*100)),done:current>=ch.target}}

function getSuggestedConnections(bookId){
  const book=data.books.find(b=>b.id===bookId);if(!book)return[];
  const existing=new Set(book.connections||[]);
  return data.books.filter(b=>b.id!==bookId&&!existing.has(b.id)).map(b=>{
    let score=0;const sharedThemes=(book.themes||[]).filter(t=>(b.themes||[]).includes(t));score+=sharedThemes.length*3;if(b.category===book.category)score+=2;
    if(book.startDate&&b.startDate){const s1=new Date(book.startDate),e1=book.endDate?new Date(book.endDate):new Date(),s2=new Date(b.startDate),e2=b.endDate?new Date(b.endDate):new Date();if(s1<=e2&&s2<=e1)score+=1}
    return{book:b,score,sharedThemes};
  }).filter(x=>x.score>0).sort((a,b)=>b.score-a.score).slice(0,5);
}

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
books[0].connections=[books[1].id];books[1].connections=[books[0].id];books[3].connections=[books[2].id,books[5].id];
return{books,settings:{goal:50,lastExport:null,goals:{annual:50,categories:{},challenges:[]}}};
}

data=load()||makeDemoData();
if(!data.settings.goals)data.settings.goals={annual:data.settings.goal||50,categories:{},challenges:[]};
if(!data.settings.goals.challenges)data.settings.goals.challenges=[];
data.books.forEach(b=>{if(!b.sessions)b.sessions=[];if(!b.themes)b.themes=[];if(!b.quotes)b.quotes=[];if(!b.connections)b.connections=[];if(b.priority===undefined)b.priority=0;if(!b.recommendedBy)b.recommendedBy="";if(!b.recommendationNote)b.recommendationNote="";if(!b.recommendationSource)b.recommendationSource=""});
save();pickRandomQuote();

// ─── Toast ───
