package com.readingledger.app.db;

import com.readingledger.app.util.C;

public class DemoData {
    public static String getJson() {
        String now = C.isoNow();
        return "{\"books\":["
            + book("Atomic Habits","James Clear","Self-Awareness","completed",320,320,5,"2025-01-05","2025-01-18","Remarkable framework for behavior change.","[\"habits\",\"systems\",\"identity\"]","[{\"text\":\"You do not rise to the level of your goals. You fall to the level of your systems.\",\"page\":\"27\"},{\"text\":\"Every action you take is a vote for the type of person you wish to become.\",\"page\":\"38\"}]","[{\"date\":\"2025-01-08\",\"startPage\":0,\"endPage\":100,\"duration\":60},{\"date\":\"2025-01-12\",\"startPage\":100,\"endPage\":220,\"duration\":75},{\"date\":\"2025-01-17\",\"startPage\":220,\"endPage\":320,\"duration\":55}]","Small habits compound into remarkable results over time.","Changed how I think about daily routines.","2025-01-05T10:00:00Z",now) + ","
            + book("Thinking, Fast and Slow","Daniel Kahneman","Science","completed",499,499,5,"2025-02-01","2025-03-02","Dense but essential on cognitive biases.","[\"cognition\",\"bias\",\"decision-making\"]","[{\"text\":\"Nothing in life is as important as you think it is, while you are thinking about it.\",\"page\":\"402\"}]","[{\"date\":\"2025-02-10\",\"startPage\":0,\"endPage\":180,\"duration\":120},{\"date\":\"2025-02-20\",\"startPage\":180,\"endPage\":350,\"duration\":90},{\"date\":\"2025-03-01\",\"startPage\":350,\"endPage\":499,\"duration\":80}]","Two systems govern thought: fast intuition and slow deliberation.","","2025-02-01T10:00:00Z",now) + ","
            + book("The Wealth of Nations","Adam Smith","Economics & Money","completed",1264,1264,4,"2024-11-01","2025-01-15","Foundational text of modern economics.","[\"capitalism\",\"markets\",\"labor\"]","[]","[]","Free markets guided by self-interest produce collective prosperity.","","2024-11-01T10:00:00Z",now) + ","
            + book("Sapiens","Yuval Noah Harari","World History","completed",443,443,4,"2025-03-10","2025-04-05","Sweeping narrative of human history.","[\"civilization\",\"evolution\",\"narrative\"]","[{\"text\":\"The real difference between us and chimpanzees is the mythical glue that binds together large numbers of individuals.\",\"page\":\"25\"}]","[{\"date\":\"2025-03-15\",\"startPage\":0,\"endPage\":150,\"duration\":90},{\"date\":\"2025-03-25\",\"startPage\":150,\"endPage\":320,\"duration\":100},{\"date\":\"2025-04-04\",\"startPage\":320,\"endPage\":443,\"duration\":70}]","Shared myths enable human cooperation at scale.","Reframed how I understand institutions.","2025-03-10T10:00:00Z",now) + ","
            + book("Meditations","Marcus Aurelius","Philosophy & Ethics","completed",256,256,5,"2025-04-10","2025-04-20","Timeless. Every page has something to sit with.","[\"stoicism\",\"virtue\",\"mortality\"]","[{\"text\":\"The happiness of your life depends upon the quality of your thoughts.\",\"page\":\"\"}]","[{\"date\":\"2025-04-12\",\"startPage\":0,\"endPage\":128,\"duration\":45},{\"date\":\"2025-04-19\",\"startPage\":128,\"endPage\":256,\"duration\":50}]","Inner peace through acceptance of what you cannot control.","Daily reference now.","2025-04-10T10:00:00Z",now) + ","
            + book("1984","George Orwell","Fiction","completed",328,328,4,"2025-05-01","2025-05-12","Chillingly relevant.","[\"totalitarianism\",\"surveillance\",\"language\"]","[]","[{\"date\":\"2025-05-05\",\"startPage\":0,\"endPage\":160,\"duration\":80},{\"date\":\"2025-05-11\",\"startPage\":160,\"endPage\":328,\"duration\":85}]","","","2025-05-01T10:00:00Z",now) + ","
            + book("The Gene","Siddhartha Mukherjee","Science","reading",592,340,0,"2026-02-15","","Beautifully written history of genetics.","[\"genetics\",\"science\",\"ethics\"]","[]","[{\"date\":\"2026-02-20\",\"startPage\":0,\"endPage\":120,\"duration\":90},{\"date\":\"2026-02-25\",\"startPage\":120,\"endPage\":240,\"duration\":75},{\"date\":\"2026-03-05\",\"startPage\":240,\"endPage\":340,\"duration\":60}]","","","2026-02-15T10:00:00Z",now) + ","
            + book("Democracy in America","Alexis de Tocqueville","American History","reading",864,210,0,"2026-03-01","","","[\"democracy\",\"America\",\"institutions\"]","[]","[{\"date\":\"2026-03-05\",\"startPage\":0,\"endPage\":100,\"duration\":120},{\"date\":\"2026-03-09\",\"startPage\":100,\"endPage\":210,\"duration\":80}]","","","2026-03-01T10:00:00Z",now) + ","
            + book("The Innovators","Walter Isaacson","Technology","want-to-read",542,0,0,"","","","[]","[]","[]","","","2026-01-15T10:00:00Z",now) + ","
            + book("The Righteous Mind","Jonathan Haidt","Current America","want-to-read",419,0,0,"","","","[]","[]","[]","","","2026-02-01T10:00:00Z",now) + ","
            + book("The Brothers Karamazov","Fyodor Dostoevsky","Fiction","want-to-read",796,0,0,"","","","[]","[]","[]","","","2026-03-01T10:00:00Z",now) + ","
            + book("Being and Time","Martin Heidegger","Philosophy & Ethics","abandoned",589,95,2,"2025-06-01","2025-06-20","Too dense. Will revisit.","[\"existentialism\"]","[]","[]","","","2025-06-01T10:00:00Z",now)
            + "],\"settings\":{\"goal\":50,\"lastExport\":null,\"goals\":{\"annual\":50,\"categories\":{},\"challenges\":[]}}}";
    }

    private static String book(String title, String author, String category, String status,
                                int pages, int currentPage, int rating, String start, String end,
                                String notes, String themes, String quotes, String sessions,
                                String core, String impact, String addedAt, String updatedAt) {
        return "{\"id\":\"" + C.uid() + "\",\"title\":\"" + esc(title) + "\",\"author\":\"" + esc(author)
             + "\",\"category\":\"" + category + "\",\"status\":\"" + status
             + "\",\"pages\":" + pages + ",\"currentPage\":" + currentPage + ",\"rating\":" + rating
             + ",\"startDate\":" + jsonStr(start) + ",\"endDate\":" + jsonStr(end)
             + ",\"notes\":\"" + esc(notes) + "\",\"themes\":" + themes + ",\"quotes\":" + quotes
             + ",\"connections\":[],\"sessions\":" + sessions
             + ",\"coreArgument\":\"" + esc(core) + "\",\"impact\":\"" + esc(impact)
             + "\",\"recommendedBy\":\"\",\"recommendationNote\":\"\",\"recommendationSource\":\"\",\"priority\":0"
             + ",\"addedAt\":\"" + addedAt + "\",\"updatedAt\":\"" + updatedAt + "\"}";
    }

    private static String esc(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    private static String jsonStr(String s) {
        return (s == null || s.isEmpty()) ? "null" : "\"" + s + "\"";
    }
}
