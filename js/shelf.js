/* ═══ SHELF — Shelf rendering, spine layout, Now Reading cards, events ═══ */
// ══════ SHELF ══════
function renderShelf(){
  const books=searchQuery?data.books.filter(b=>{const q=searchQuery.toLowerCase();return(b.title||"").toLowerCase().includes(q)||(b.author||"").toLowerCase().includes(q)||(b.category||"").toLowerCase().includes(q)||(b.notes||"").toLowerCase().includes(q)||(b.themes||[]).some(t=>t.toLowerCase().includes(q))||(b.quotes||[]).some(qt=>(qt.text||"").toLowerCase().includes(q))}):data.books;
  let html="";
  // Nudge banner
  const nudge=getNudge();
  if(nudge&&!searchQuery)
    html+=`<div class="nudge-banner" id="nudgeBanner" data-id="${nudge.bookId}"><span class="nudge-icon">${nudge.icon}</span><div class="nudge-body"><p class="nudge-title">${esc(nudge.title)} \u2014 ${nudge.pct}%</p><p class="nudge-sub">${nudge.sub}</p></div><button class="nudge-dismiss" id="nudgeDismiss">\u2715</button></div>`;
  // Backup reminder
  const lastExp=data.settings.lastExport?new Date(data.settings.lastExport):null;
  const daysSinceExport=lastExp?Math.floor((Date.now()-lastExp.getTime())/86400000):999;
  if(daysSinceExport>30&&data.books.length>0&&!searchQuery)
    html+=`<div class="backup-banner" id="backupBanner"><span style="font-size:16px">\u26A0</span><span style="flex:1">It\u2019s been ${lastExp?daysSinceExport+" days":"a while"} since your last backup.</span><button class="bb-export" id="bbExport">Export</button><button class="bb-dismiss" id="bbDismiss">\u2715</button></div>`;
  // Quote banner
  if(randomQuote&&!searchQuery)
    html+=`<div class="quote-banner"><div style="flex-shrink:0;color:var(--accentDim);margin-top:2px">\u201C</div><div style="flex:1"><p class="quote-banner-text">${esc(randomQuote.text)}</p><p class="quote-banner-src">\u2014 ${esc(randomQuote.bookTitle)}${randomQuote.page?" \u00B7 p. "+esc(randomQuote.page):""}</p></div><button class="quote-refresh" id="quoteRefresh">\u21BB</button></div>`;
  // Milestones
  const ms=getMilestones();
  if(ms.length&&!searchQuery) html+=`<div class="milestones">${ms.slice(-4).map(m=>`<div class="milestone"><span>${m.icon}</span> ${m.label}</div>`).join("")}</div>`;
  // Sort bar
  const sorts=[["date","Date"],["category","Category"],["rating","Rating"],["length","Length"],["status","Status"],["title","Title"]];
  html+=`<div class="shelf-header"><p class="shelf-count">${books.length} book${books.length!==1?"s":""}</p><div class="sort-bar">${sorts.map(([k,l])=>`<button class="sort-pill${currentSort===k?" active":""}" data-sort="${k}">${l}</button>`).join("")}</div></div>`;
  if(!books.length){
    html+=`<div class="empty"><p class="title">Your shelves are empty.</p><p class="sub">Add your first book to begin.</p><button class="empty-cta" onclick="openForm(null)">+ Add a Book</button></div>`;
  } else {
    const sorted=[...books].sort((a,b)=>{switch(currentSort){case"date":return new Date(b.addedAt||0)-new Date(a.addedAt||0);case"category":return(a.category||"").localeCompare(b.category||"");case"rating":return(b.rating||0)-(a.rating||0);case"length":return(b.pages||0)-(a.pages||0);case"status":{const o={reading:0,completed:1,"want-to-read":2,abandoned:3};return(o[a.status]??4)-(o[b.status]??4)}case"title":return(a.title||"").localeCompare(b.title||"");default:return 0}});
    for(let i=0;i<sorted.length;i+=6){const row=sorted.slice(i,i+6);
      html+=`<div class="shelf-row-wrap"><div class="shelf-spines">`;
      row.forEach(bk=>{const sc=CAT_SPINE[bk.category]||CAT_SPINE.Other,lc=CAT_COLORS[bk.category]||CAT_COLORS.Other;
        html+=`<div class="spine" data-id="${bk.id}" style="width:52px;height:130px;background:linear-gradient(145deg,${sc},${lc}80);border:1px solid ${lc}25;box-shadow:1px 2px 5px rgba(0,0,0,0.5)"><div class="sheen"></div>`;
        if(bk.rating>=4)html+=`<div class="gold-top"></div>`;
        html+=`<span class="spine-title" style="color:rgba(255,255,255,0.92);max-height:110px">${esc(bk.title)}</span>`;
        if(bk.status==="reading")html+=`<div class="reading-dot"></div>`;
        if(bk.status==="want-to-read")html+=`<div class="want-mark"></div>`;
        html+=`</div>`});
      html+=`</div><div class="shelf-board"></div></div>`}
    const usedCats=[...new Set(books.map(b=>b.category||"Other"))].sort();
    html+=`<div class="shelf-footer"><strong style="color:var(--text)">${books.length}</strong> books &middot; <span style="color:var(--green)">${books.filter(b=>b.status==="completed").length}</span> read &middot; <span style="color:var(--blue)">${books.filter(b=>b.status==="reading").length}</span> reading &middot; <span style="color:var(--textD)">${books.filter(b=>b.status==="want-to-read").length}</span> want<div class="shelf-footer-legend">${usedCats.map(c=>`<span class="shelf-footer-legend-item"><span class="shelf-footer-legend-dot" style="background:${CAT_COLORS[c]||CAT_COLORS.Other}"></span>${c}</span>`).join("")}</div></div>`;
  }
  // Now Reading strip (enhanced)
  const nr=data.books.filter(b=>b.status==="reading");
  if(nr.length&&!searchQuery){
    html+=`<div class="nr-section"><p class="nr-section-title">Now Reading</p>`;
    nr.forEach(b=>{
      const col=CAT_COLORS[b.category]||CAT_COLORS.Other,pg=b.pages||1,cur=b.currentPage||0,pct=Math.min(100,Math.round(cur/pg*100));
      html+=`<div class="nr-card" data-book="${b.id}"><div class="nr-card-header"><div class="nr-card-accent" style="background:linear-gradient(180deg,${col},${col}80)"></div><div class="nr-card-info"><div class="nr-card-title" data-id="${b.id}">${esc(b.title)}</div><div class="nr-card-author">${esc(b.author||'')}</div></div><span class="nr-card-pct" style="color:${col}" id="nrPct_${b.id}">${pct}%</span></div>`;
      html+=`<div class="nr-bar"><div class="nr-bar-fill" id="nrFill_${b.id}" style="width:${pct}%;background:linear-gradient(90deg,${col}90,${col})"></div></div>`;
      html+=`<div class="nr-stepper" data-book="${b.id}" data-pages="${pg}"><button data-inc="-10">\u221210</button><button data-inc="-1">\u22121</button><div class="nr-val"><input type="number" value="${cur}" min="0" max="${pg}" data-book="${b.id}"><span>${cur}</span></div><button data-inc="1">+1</button><button data-inc="10">+10</button></div>`;
      html+=`<div class="nr-of">of ${pg} pages</div>`;
      html+=`<div class="nr-pending" id="nrPending_${b.id}"></div>`;
      html+=`</div>`;
    });
    html+=`</div>`;
  }
  document.getElementById("viewShelf").innerHTML=html;
  bindShelfEvents();
}

function bindShelfEvents(){
  document.querySelectorAll("#viewShelf .sort-pill").forEach(b=>b.onclick=()=>{currentSort=b.dataset.sort;renderShelf()});
  // Spine → direct to detail (skip tooltip)
  document.querySelectorAll("#viewShelf .spine").forEach(s=>s.onclick=e=>{e.stopPropagation();openDetail(s.dataset.id)});
  const qr=document.getElementById("quoteRefresh");if(qr)qr.onclick=()=>{pickRandomQuote();renderShelf()};
  // Backup banner
  const bbExp=document.getElementById("bbExport");if(bbExp)bbExp.onclick=()=>{const json=JSON.stringify(data,null,2);const b=new Blob([json],{type:"application/json"}),a=document.createElement("a");a.href=URL.createObjectURL(b);a.download=`reading-ledger-backup-${new Date().toISOString().slice(0,10)}.json`;a.click();URL.revokeObjectURL(a.href);data.settings.lastExport=new Date().toISOString();save();renderShelf()};
  const bbDis=document.getElementById("bbDismiss");if(bbDis)bbDis.onclick=()=>{document.getElementById("backupBanner")?.remove()};
  // Now Reading events
  document.querySelectorAll("#viewShelf .nr-card-title[data-id]").forEach(t=>t.onclick=()=>openDetail(t.dataset.id));
  // Stepper buttons
  document.querySelectorAll("#viewShelf .nr-stepper button[data-inc]").forEach(btn=>{const stepper=btn.closest(".nr-stepper");const bookId=stepper.dataset.book;btn.onclick=()=>{const bk=data.books.find(b=>b.id===bookId);if(!bk)return;nrMovePage(bookId,bk.currentPage+parseInt(btn.dataset.inc))}});
  // Stepper direct input
  document.querySelectorAll("#viewShelf .nr-stepper .nr-val input").forEach(inp=>{inp.onfocus=()=>inp.select();inp.onblur=()=>{nrMovePage(inp.dataset.book,parseInt(inp.value)||0);inp.style.opacity="0";inp.nextElementSibling.style.opacity="1"};inp.onkeydown=e=>{if(e.key==="Enter")inp.blur()}});
  // Nudge banner
  const nudgeBanner=document.getElementById("nudgeBanner");if(nudgeBanner)nudgeBanner.onclick=e=>{if(e.target.id==="nudgeDismiss"){nudgeDismissed=true;renderShelf();return}openDetail(nudgeBanner.dataset.id)};
}
