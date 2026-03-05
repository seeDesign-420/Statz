"use client";

import { useState } from "react";
import {
  BarChart2,
  Headphones,
  CheckSquare,
  Settings,
  ChevronLeft,
  ChevronRight,
  CalendarDays,
  ArrowLeft,
  Search,
  Plus,
  Send,
  X,
  Trash2,
  Flag,
  Bell,
  User,
  Target,
  Moon,
  LogOut,
  ChevronRight as ChevronRightIcon,
  HelpCircle,
  FileText
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function App() {
  const [activeTab, setActiveTab] = useState("sales");
  const [currentScreen, setCurrentScreen] = useState("main"); // main, daily-entry, edit-targets, new-query, query-detail, task-detail

  if (currentScreen === "daily-entry") return <DailyEntry onBack={() => setCurrentScreen("main")} />;
  if (currentScreen === "edit-targets") return <EditTargets onBack={() => setCurrentScreen("main")} />;
  if (currentScreen === "new-query") return <NewQuery onBack={() => setCurrentScreen("main")} />;
  if (currentScreen === "query-detail") return <QueryDetail onBack={() => setCurrentScreen("main")} />;
  if (currentScreen === "task-detail") return <TaskDetail onBack={() => setCurrentScreen("main")} />;

  return (
    <div className="flex flex-col h-[100dvh] bg-background text-foreground overflow-hidden">
      <main className="flex-1 overflow-y-auto pb-24">
        {activeTab === "sales" && <SalesDashboard onNavigate={setCurrentScreen} />}
        {activeTab === "queries" && <QueriesList onNavigate={setCurrentScreen} />}
        {activeTab === "todo" && <TodoList onNavigate={setCurrentScreen} />}
        {activeTab === "settings" && <SettingsScreen onNavigate={setCurrentScreen} />}
      </main>

      <nav className="fixed bottom-0 w-full h-[88px] bg-card border-t border-border flex items-center justify-around px-2 pb-safe z-50">
        <NavItem
          icon={<BarChart2 className="w-6 h-6" />}
          label="Sales Stats"
          isActive={activeTab === "sales"}
          onClick={() => setActiveTab("sales")}
        />
        <NavItem
          icon={<Headphones className="w-6 h-6" />}
          label="Queries"
          isActive={activeTab === "queries"}
          onClick={() => setActiveTab("queries")}
        />
        <NavItem
          icon={<CheckSquare className="w-6 h-6" />}
          label="To-Do"
          isActive={activeTab === "todo"}
          onClick={() => setActiveTab("todo")}
        />
        <NavItem
          icon={<Settings className="w-6 h-6" />}
          label="Settings"
          isActive={activeTab === "settings"}
          onClick={() => setActiveTab("settings")}
        />
      </nav>
    </div>
  );
}

function NavItem({ icon, label, isActive, onClick }: { icon: React.ReactNode, label: string, isActive: boolean, onClick: () => void }) {
  return (
    <button
      onClick={onClick}
      className={`relative flex flex-col items-center justify-center w-full h-full gap-1 transition-colors ${isActive ? "text-primary" : "text-muted-foreground hover:text-foreground"
        }`}
    >
      <div className={`p-1.5 rounded-full transition-all duration-300 ${isActive ? "scale-110" : "scale-100"}`}>
        {icon}
      </div>
      <span className="text-[11px] font-medium tracking-wide">{label}</span>
      <div
        className={`absolute top-0 w-12 h-1 rounded-b-full bg-primary transition-all duration-300 ${isActive ? "opacity-100 scale-x-100" : "opacity-0 scale-x-0"}`}
      />
    </button>
  );
}

// ------------------------------------------------------------------------------------------------
// TAB 1: SALES DASHBOARD & SUB-SCREENS
// ------------------------------------------------------------------------------------------------

function SalesDashboard({ onNavigate }: { onNavigate: (screen: string) => void }) {
  return (
    <div className="flex flex-col min-h-full">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80 border-b border-outline px-4 py-3 flex items-center justify-between">
        <Button variant="ghost" size="icon" className="h-10 w-10 text-muted-foreground rounded-full hover:bg-accent/50">
          <ChevronLeft className="w-6 h-6" />
        </Button>
        <div className="flex items-center gap-2 font-mono text-lg font-semibold tracking-tight">
          March 2026
          <CalendarDays className="w-4 h-4 text-muted-foreground" />
        </div>
        <Button variant="ghost" size="icon" className="h-10 w-10 text-muted-foreground rounded-full hover:bg-accent/50">
          <ChevronRight className="w-6 h-6" />
        </Button>
      </header>

      <div className="p-4 space-y-8">
        <div className="grid grid-cols-2 gap-4">
          <Card className="border-border/50 bg-card overflow-hidden relative">
            <div className="absolute top-0 left-0 w-full h-[2px] bg-primary/40" />
            <CardContent className="p-5 flex flex-col items-center justify-center text-center gap-2">
              <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Total Units</span>
              <span className="text-4xl font-mono text-foreground font-bold leading-none">47</span>
            </CardContent>
          </Card>
          <Card className="border-border/50 bg-card overflow-hidden relative">
            <div className="absolute top-0 left-0 w-full h-[2px] bg-primary/40" />
            <CardContent className="p-5 flex flex-col items-center justify-center text-center gap-2">
              <span className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Total Revenue</span>
              <span className="text-2xl font-mono text-foreground font-bold tracking-tighter leading-tight mt-1">R 12,450</span>
            </CardContent>
          </Card>
        </div>

        <div className="space-y-5">
          <h3 className="text-[13px] font-semibold text-muted-foreground uppercase tracking-widest pl-1">Unit Categories</h3>
          <div className="space-y-4">
            <CategoryRow name="Fiber" target={15} actual={12} isMoney={false} />
            <CategoryRow name="Mobile Plans" target={30} actual={25} isMoney={false} />
            <CategoryRow name="Hardware" target={10} actual={10} isMoney={false} />
            <CategoryRow name="SME Upgrades" target={5} actual={2} isMoney={false} />
          </div>
        </div>

        <div className="space-y-5 pt-2">
          <h3 className="text-[13px] font-semibold text-muted-foreground uppercase tracking-widest pl-1">Revenue Categories</h3>
          <div className="space-y-4">
            <CategoryRow name="Accessories" target={5000} actual={2450} isMoney={true} />
            <CategoryRow name="Cash Sales" target={10000} actual={10000} isMoney={true} />
          </div>
        </div>
      </div>

      <div className="fixed bottom-[104px] right-4 z-50">
        <Button
          onClick={() => onNavigate("daily-entry")}
          size="icon"
          className="w-[56px] h-[56px] rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.4)] shadow-primary/30 bg-primary hover:bg-primary/90 hover:scale-105 transition-all"
        >
          <CalendarDays className="w-6 h-6 text-primary-foreground" />
        </Button>
      </div>
    </div>
  );
}

function CategoryRow({ name, target, actual, isMoney }: { name: string, target: number, actual: number, isMoney: boolean }) {
  const percentage = Math.min(100, (actual / target) * 100);
  const isComplete = actual >= target;

  return (
    <div className="flex flex-col gap-2.5 group">
      <div className="flex items-center justify-between">
        <span className="font-medium text-foreground text-sm tracking-wide">{name}</span>
        <div className="flex items-center gap-4">
          <span className="font-mono text-sm text-muted-foreground">
            {isMoney ? `R ${actual.toLocaleString()}` : actual} <span className="opacity-50">/</span> {isMoney ? `R ${target.toLocaleString()}` : target}
          </span>
          <Button size="icon" variant="outline" className="w-8 h-8 rounded-full border-border/60 text-foreground hover:bg-accent/50 hover:text-primary transition-colors flex-shrink-0">
            {isMoney ? <span className="text-xs font-mono font-bold">+R</span> : <span className="text-[10px] font-mono font-bold">+1</span>}
          </Button>
        </div>
      </div>
      <div className="h-[6px] w-full bg-accent/50 rounded-full overflow-hidden">
        <div
          className={`h-full rounded-full transition-all duration-1000 ease-out ${isComplete ? 'bg-[#22C55E]' : 'bg-secondary'}`}
          style={{ width: `${percentage}%` }}
        />
      </div>
    </div>
  );
}

function DailyEntry({ onBack }: { onBack: () => void }) {
  return (
    <div className="flex flex-col min-h-[100dvh] bg-background text-foreground">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button onClick={onBack} variant="ghost" size="icon" className="h-10 w-10 -ml-2 text-muted-foreground rounded-full hover:bg-accent/50">
            <ArrowLeft className="w-6 h-6" />
          </Button>
          <span className="text-lg font-bold tracking-tight">Daily Entry</span>
        </div>
        <div className="bg-primary/10 text-primary px-3 py-1.5 rounded-full text-xs font-semibold tracking-wide border border-primary/20">
          Today — 03 Mar
        </div>
      </header>

      <main className="flex-1 p-5 space-y-8 overflow-y-auto pb-32">
        <section className="space-y-4">
          <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-widest pl-1">Unit Sales</h3>
          <div className="grid grid-cols-2 gap-4">
            <FormNumberInput label="New Plans" />
            <FormNumberInput label="Upgrades" />
            <FormNumberInput label="SME New" />
            <FormNumberInput label="SME Up" />
            <FormNumberInput label="EC New" />
            <FormNumberInput label="EC Up" />
            <FormNumberInput label="Fiber" />
            <FormNumberInput label="HW Cont" />
            <FormNumberInput label="HW MTM" />
            <FormNumberInput label="Insurance" />
          </div>
        </section>

        <section className="space-y-4">
          <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-widest pl-1">Revenue</h3>
          <div className="grid gap-4">
            <FormMoneyInput label="Accessories" />
            <FormMoneyInput label="Cash Sales" />
          </div>
        </section>

        <section className="space-y-4">
          <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-widest pl-1">Open & Declined</h3>
          <div className="grid grid-cols-2 gap-4">
            <FormNumberInput label="Open New" />
            <FormNumberInput label="Open Upg" />
            <FormNumberInput label="Decl New" />
            <FormNumberInput label="Decl Upg" />
          </div>
        </section>
      </main>

      <div className="fixed bottom-0 w-full p-4 bg-background/95 backdrop-blur border-t border-border z-50">
        <Button onClick={onBack} className="w-full h-14 text-[15px] font-bold tracking-wide rounded-xl shadow-lg shadow-primary/20 bg-primary hover:bg-primary/90">
          Save Entry
        </Button>
      </div>
    </div>
  );
}

function EditTargets({ onBack }: { onBack: () => void }) {
  return (
    <div className="flex flex-col min-h-[100dvh] bg-background text-foreground">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button onClick={onBack} variant="ghost" size="icon" className="h-10 w-10 -ml-2 text-muted-foreground rounded-full hover:bg-accent/50">
            <ArrowLeft className="w-6 h-6" />
          </Button>
          <span className="text-lg font-bold tracking-tight">Edit Targets</span>
        </div>
        <div className="bg-primary/10 text-primary px-3 py-1.5 rounded-full text-xs font-semibold tracking-wide border border-primary/20">
          March 2026
        </div>
      </header>

      <main className="flex-1 p-5 space-y-8 overflow-y-auto pb-32">
        <section className="space-y-4">
          <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-widest pl-1">Unit Targets</h3>
          <div className="space-y-3">
            <TargetInputRow label="New Plans" defaultValue="15" />
            <TargetInputRow label="Upgrades" defaultValue="30" />
            <TargetInputRow label="SME New" defaultValue="5" />
            <TargetInputRow label="SME Up" defaultValue="10" />
            <TargetInputRow label="EC New" defaultValue="2" />
            <TargetInputRow label="EC Up" defaultValue="5" />
            <TargetInputRow label="Fiber" defaultValue="15" />
            <TargetInputRow label="Hardware" defaultValue="20" />
          </div>
        </section>

        <section className="space-y-4">
          <h3 className="text-sm font-semibold text-muted-foreground uppercase tracking-widest pl-1">Revenue Targets</h3>
          <div className="space-y-3">
            <TargetInputRow label="Accessories" defaultValue="5000" isMoney={true} />
            <TargetInputRow label="Cash Sales" defaultValue="10000" isMoney={true} />
          </div>
        </section>
      </main>

      <div className="fixed bottom-0 w-full p-4 bg-background/95 backdrop-blur border-t border-border z-50">
        <Button onClick={onBack} variant="secondary" className="w-full h-14 text-[15px] font-bold tracking-wide rounded-xl bg-secondary text-secondary-foreground hover:bg-secondary/80">
          Save All Targets
        </Button>
      </div>
    </div>
  );
}

function TargetInputRow({ label, defaultValue, isMoney }: { label: string, defaultValue: string, isMoney?: boolean }) {
  return (
    <div className="flex items-center justify-between p-3 rounded-xl bg-card/50 border border-border/50">
      <Label className="text-sm font-semibold">{label}</Label>
      <div className="relative w-32">
        {isMoney && <div className="absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground font-mono font-bold text-sm">R</div>}
        <Input
          type="number"
          defaultValue={defaultValue}
          className={`h-10 bg-background border-border/80 text-right font-mono text-sm placeholder:text-muted-foreground/50 rounded-lg focus-visible:ring-primary focus-visible:border-primary transition-all ${isMoney ? 'pl-8' : ''}`}
        />
      </div>
    </div>
  );
}

function FormNumberInput({ label }: { label: string }) {
  return (
    <div className="flex flex-col gap-2">
      <Label className="text-xs text-muted-foreground font-medium pl-1">{label}</Label>
      <Input
        type="number"
        defaultValue="0"
        className="h-14 bg-card/50 border-border/80 text-lg font-mono placeholder:text-muted-foreground/50 rounded-xl focus-visible:ring-primary focus-visible:border-primary transition-all"
      />
    </div>
  );
}

function FormMoneyInput({ label }: { label: string }) {
  return (
    <div className="flex flex-col gap-2">
      <Label className="text-xs text-muted-foreground font-medium pl-1">{label}</Label>
      <div className="relative">
        <div className="absolute left-4 top-1/2 -translate-y-1/2 text-muted-foreground font-mono font-bold">R</div>
        <Input
          type="number"
          defaultValue="0.00"
          className="h-14 pl-8 bg-card/50 border-border/80 text-lg font-mono placeholder:text-muted-foreground/50 rounded-xl focus-visible:ring-primary focus-visible:border-primary transition-all"
        />
      </div>
    </div>
  );
}

// ------------------------------------------------------------------------------------------------
// TAB 2: QUERIES & SUB-SCREENS
// ------------------------------------------------------------------------------------------------

function QueriesList({ onNavigate }: { onNavigate: (screen: string) => void }) {
  const [activeFilter, setActiveFilter] = useState("Open");
  const filters = ["All", "Open", "Follow-Up", "Escalated", "Closed"];

  return (
    <div className="flex flex-col min-h-full">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border py-4 flex flex-col gap-4">
        <div className="flex items-center justify-between px-4">
          <span className="text-2xl font-bold tracking-tight">Queries</span>
          <Button variant="ghost" size="icon" className="h-10 w-10 text-foreground rounded-full hover:bg-accent/50">
            <Search className="w-5 h-5" />
          </Button>
        </div>
        <div className="flex gap-2 overflow-x-auto px-4 pb-1 no-scrollbar flex-nowrap">
          {filters.map(filter => (
            <button
              key={filter}
              onClick={() => setActiveFilter(filter)}
              className={`whitespace-nowrap px-4 py-1.5 rounded-full text-xs font-semibold tracking-wide border transition-all ${activeFilter === filter
                  ? 'bg-foreground text-background border-foreground'
                  : 'bg-card/50 text-muted-foreground border-border hover:border-muted-foreground hover:text-foreground'
                }`}
            >
              {filter}
            </button>
          ))}
        </div>
      </header>

      <div className="p-4 space-y-3">
        <QueryCard id="QRY-4521" name="John Doe" status="Open" urgency="High" time="Today 14:00" onClick={() => onNavigate("query-detail")} />
        <QueryCard id="QRY-4519" name="Alice Smith" status="Follow-Up" urgency="Medium" time="Tomorrow 09:00" onClick={() => onNavigate("query-detail")} />
        <QueryCard id="QRY-4490" name="Bob Johnson" status="Escalated" urgency="Critical" time="Overdue" onClick={() => onNavigate("query-detail")} />
      </div>

      <div className="fixed bottom-[104px] right-4 z-50">
        <Button
          onClick={() => onNavigate("new-query")}
          size="icon"
          className="w-[56px] h-[56px] rounded-2xl shadow-[0_8px_30px_rgb(0,0,0,0.4)] shadow-secondary/30 bg-secondary hover:bg-secondary/90 hover:scale-105 transition-all text-secondary-foreground"
        >
          <Plus className="w-6 h-6" />
        </Button>
      </div>
    </div>
  );
}

function QueryCard({ id, name, status, urgency, time, onClick }: any) {
  let urgencyColor = "bg-slate-500/20 text-slate-400";
  if (urgency === "Medium") urgencyColor = "bg-amber-500/20 text-amber-500 border-amber-500/30";
  if (urgency === "High") urgencyColor = "bg-orange-500/20 text-orange-500 border-orange-500/30";
  if (urgency === "Critical") urgencyColor = "bg-red-500/20 text-red-500 border-red-500/30";

  let statusColor = "border-border text-muted-foreground";
  if (status === "Open") statusColor = "border-primary text-primary bg-primary/10";
  if (status === "Follow-Up") statusColor = "border-amber-500 text-amber-500 bg-amber-500/10";
  if (status === "Escalated") statusColor = "border-red-500 text-red-500 bg-red-500/10";

  return (
    <Card onClick={onClick} className="border border-border/60 bg-card hover:bg-card/80 transition-colors cursor-pointer group shadow-sm rounded-2xl">
      <CardContent className="p-4 flex flex-col gap-3">
        <div className="flex justify-between items-start">
          <div className="flex flex-col gap-1">
            <span className="font-mono font-bold tracking-tight text-foreground group-hover:text-primary transition-colors">{id}</span>
            <span className="text-sm font-medium text-muted-foreground">{name}</span>
          </div>
          <div className={`px-2.5 py-1 rounded-md text-[10px] font-bold uppercase tracking-wider border ${urgencyColor}`}>
            {urgency}
          </div>
        </div>
        <div className="flex justify-between items-center mt-2">
          <div className={`px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border ${statusColor}`}>
            {status}
          </div>
          <span className={`text-xs font-mono font-medium ${time === 'Overdue' ? 'text-red-500' : 'text-muted-foreground'}`}>{time !== 'Overdue' && 'Next: '}{time}</span>
        </div>
      </CardContent>
    </Card>
  );
}

function QueryDetail({ onBack }: { onBack: () => void }) {
  return (
    <div className="flex flex-col min-h-[100dvh] bg-background text-foreground">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button onClick={onBack} variant="ghost" size="icon" className="h-10 w-10 -ml-2 text-muted-foreground rounded-full hover:bg-accent/50">
            <ArrowLeft className="w-6 h-6" />
          </Button>
          <span className="font-mono text-lg font-bold tracking-tight">#QRY-4521</span>
        </div>
        <Button variant="ghost" size="icon" className="h-10 w-10 text-muted-foreground rounded-full hover:bg-accent/50">
          <Settings className="w-5 h-5" />
        </Button>
      </header>

      <main className="flex-1 p-4 space-y-6 overflow-y-auto pb-32">
        <Card className="border border-border/60 bg-card rounded-2xl shadow-sm">
          <CardContent className="p-5 space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] uppercase tracking-widest text-muted-foreground font-semibold">Customer Name</span>
                <span className="font-medium text-foreground">John Doe</span>
              </div>
              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] uppercase tracking-widest text-muted-foreground font-semibold">Customer ID</span>
                <span className="font-mono text-foreground">CUST-8820</span>
              </div>
            </div>
            <div className="flex items-center gap-3 pt-3 border-t border-border/50">
              <span className="text-[10px] uppercase tracking-widest text-muted-foreground font-semibold mr-2">Status</span>
              <div className="px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border border-primary text-primary bg-primary/10">
                Open
              </div>
              <div className="px-2 py-0.5 rounded text-[10px] font-bold uppercase tracking-wider border border-orange-500/30 text-orange-500 bg-orange-500/10 ml-auto">
                High Urgency
              </div>
            </div>
          </CardContent>
        </Card>

        <section className="space-y-3">
          <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-widest pl-1">Actions</h3>
          <div className="grid grid-cols-3 gap-2">
            <Button variant="outline" className="h-12 rounded-xl border-amber-500/30 text-amber-500 hover:bg-amber-500/10 font-bold text-xs tracking-wide">Follow-Up</Button>
            <Button variant="outline" className="h-12 rounded-xl border-red-500/30 text-red-500 hover:bg-red-500/10 font-bold text-xs tracking-wide">Escalate</Button>
            <Button variant="outline" className="h-12 rounded-xl border-green-500/30 text-green-500 hover:bg-green-500/10 font-bold text-xs tracking-wide">Close</Button>
          </div>
        </section>

        <section className="space-y-3">
          <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-widest pl-1 mt-6">Snooze Follow-Up</h3>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-full border-border/60 text-xs font-mono font-medium">+1h</Button>
            <Button variant="outline" className="h-9 rounded-full border-border/60 text-xs font-mono font-medium">+4h</Button>
            <Button variant="outline" className="h-9 rounded-full border-border/60 text-xs font-medium">Tomorrow 09:00</Button>
          </div>
        </section>

        <section className="space-y-3 pt-2">
          <h3 className="text-xs font-semibold text-muted-foreground uppercase tracking-widest pl-1">Log Timeline</h3>
          <div className="space-y-6 border-l-2 border-border/50 ml-2 pl-4 py-2 relative">
            <div className="relative">
              <div className="absolute -left-[21px] top-1.5 w-2.5 h-2.5 rounded-full bg-primary ring-4 ring-background" />
              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] font-mono text-muted-foreground">Today, 09:45 AM</span>
                <p className="text-sm bg-card/60 border border-border/50 p-3 rounded-xl">Customer confirmed that the LTE router is still losing connection drops. Requested a replacement unit.</p>
              </div>
            </div>
            <div className="relative">
              <div className="absolute -left-[21px] top-1.5 w-2.5 h-2.5 rounded-full bg-muted-foreground/30 ring-4 ring-background" />
              <div className="flex flex-col gap-1.5">
                <span className="text-[10px] font-mono text-muted-foreground">Yesterday, 14:20 PM</span>
                <p className="text-sm text-muted-foreground italic">System: Ticket status changed to OPEN.</p>
              </div>
            </div>
          </div>
        </section>
      </main>

      <div className="fixed bottom-0 w-full p-4 bg-background/95 backdrop-blur border-t border-border z-50 flex gap-2">
        <Input placeholder="Add a note..." className="h-12 flex-1 bg-card/50 border-border/80 rounded-xl focus-visible:ring-primary placeholder:text-muted-foreground/50" />
        <Button size="icon" className="w-12 h-12 rounded-xl shadow-md bg-primary hover:bg-primary/90 shrink-0">
          <Send className="w-5 h-5 text-primary-foreground -ml-1" />
        </Button>
      </div>
    </div>
  );
}

function NewQuery({ onBack }: { onBack: () => void }) {
  return (
    <div className="flex flex-col min-h-[100dvh] bg-background text-foreground">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button onClick={onBack} variant="ghost" size="icon" className="h-10 w-10 -ml-2 text-muted-foreground rounded-full hover:bg-accent/50">
            <X className="w-6 h-6" />
          </Button>
          <span className="text-lg font-bold tracking-tight">New Query</span>
        </div>
      </header>

      <main className="flex-1 p-5 space-y-6 overflow-y-auto pb-32">
        <section className="space-y-4">
          <div className="flex flex-col gap-2">
            <Label className="text-xs text-muted-foreground font-medium pl-1">Customer ID (Optional)</Label>
            <Input
              placeholder="e.g. CUST-1234"
              className="h-14 bg-card/50 border-border/80 font-mono text-base placeholder:text-muted-foreground/30 rounded-xl focus-visible:ring-primary focus-visible:border-primary transition-all"
            />
          </div>
          <div className="flex flex-col gap-2">
            <Label className="text-xs text-muted-foreground font-medium pl-1">Customer Name *</Label>
            <Input
              placeholder="John Doe"
              className="h-14 bg-card/50 border-border/80 text-base placeholder:text-muted-foreground/30 rounded-xl focus-visible:ring-primary focus-visible:border-primary transition-all"
            />
          </div>
        </section>

        <section className="space-y-3">
          <Label className="text-xs text-muted-foreground font-medium pl-1">Urgency</Label>
          <div className="flex gap-2 w-full">
            <Button variant="outline" className="flex-1 h-12 rounded-xl text-xs font-bold border-border/60 hover:bg-accent/50 text-muted-foreground">Low</Button>
            <Button variant="outline" className="flex-1 h-12 rounded-xl text-xs font-bold border-orange-500/40 bg-orange-500/10 text-orange-500 hover:bg-orange-500/20">Medium</Button>
            <Button variant="outline" className="flex-1 h-12 rounded-xl text-xs font-bold border-red-500/40 text-red-500 hover:bg-red-500/10">High</Button>
          </div>
        </section>

        <section className="space-y-3">
          <Label className="text-xs text-muted-foreground font-medium pl-1">Initial Note</Label>
          <textarea
            placeholder="Describe the issue..."
            className="w-full h-32 p-4 bg-card/50 border border-border/80 text-base placeholder:text-muted-foreground/30 rounded-xl focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary transition-all resize-none"
          />
        </section>
      </main>

      <div className="fixed bottom-0 w-full p-4 bg-background/95 backdrop-blur border-t border-border z-50">
        <Button onClick={onBack} className="w-full h-14 text-[15px] font-bold tracking-wide rounded-xl shadow-lg shadow-primary/20 bg-primary hover:bg-primary/90">
          Create Query
        </Button>
      </div>
    </div>
  );
}

// ------------------------------------------------------------------------------------------------
// TAB 3: TO-DO & SUB-SCREENS
// ------------------------------------------------------------------------------------------------

function TodoList({ onNavigate }: { onNavigate: (screen: string) => void }) {
  return (
    <div className="flex flex-col min-h-full">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border py-4 px-4 flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <span className="text-2xl font-bold tracking-tight">To-Do</span>
        </div>
        <div className="flex gap-2">
          <Input
            placeholder="Add a task..."
            className="h-12 flex-1 bg-card/60 border-border/80 rounded-xl placeholder:text-muted-foreground/50 border-dashed"
          />
          <Button size="icon" className="w-12 h-12 rounded-xl shadow-md bg-foreground text-background hover:bg-foreground/90 shrink-0">
            <Plus className="w-5 h-5" />
          </Button>
        </div>
      </header>

      <div className="p-4 space-y-6">
        <section className="space-y-3">
          <h3 className="text-[13px] font-bold text-red-500 uppercase tracking-widest pl-1">Overdue (1)</h3>
          <div className="space-y-2.5">
            <TaskCard
              id="1"
              title="Follow up with Alice regarding router setup"
              due="Yesterday 14:00"
              isOverdue={true}
              priority="High"
              onClick={() => onNavigate("task-detail")}
            />
          </div>
        </section>

        <section className="space-y-3">
          <h3 className="text-[13px] font-bold text-primary uppercase tracking-widest pl-1">Today (2)</h3>
          <div className="space-y-2.5">
            <TaskCard
              id="2"
              title="Submit weekly store performance report"
              due="17:00"
              isOverdue={false}
              priority="Medium"
              onClick={() => onNavigate("task-detail")}
            />
            <TaskCard
              id="3"
              title="Call back SME prospect - Retail Inc."
              due="18:30"
              isOverdue={false}
              priority="High"
              onClick={() => onNavigate("task-detail")}
            />
          </div>
        </section>

        <section className="space-y-3">
          <h3 className="text-[13px] font-bold text-muted-foreground uppercase tracking-widest pl-1">Upcoming (1)</h3>
          <div className="space-y-2.5 opacity-70">
            <TaskCard
              id="4"
              title="Update physical promotion banners"
              due="Mar 05"
              isOverdue={false}
              priority="Low"
              onClick={() => onNavigate("task-detail")}
            />
          </div>
        </section>
      </div>
    </div>
  );
}

function TaskCard({ title, due, isOverdue, priority, onClick }: any) {
  return (
    <Card onClick={onClick} className="border border-border/80 bg-card hover:bg-accent/40 transition-colors cursor-pointer group rounded-xl shadow-none">
      <CardContent className="p-3.5 flex gap-3">
        <div className="mt-0.5">
          <div className="w-5 h-5 rounded border-2 border-muted-foreground/40 group-hover:border-primary/60 transition-colors" />
        </div>
        <div className="flex flex-col flex-1 gap-1.5 min-w-0">
          <span className="text-sm font-medium text-foreground leading-snug truncate group-hover:text-primary transition-colors">{title}</span>
          <div className="flex items-center gap-3">
            <div className={`flex items-center gap-1 text-[11px] font-mono font-medium ${isOverdue ? 'text-red-500' : 'text-muted-foreground'}`}>
              <CalendarDays className="w-3 h-3" />
              {due}
            </div>
            {priority === 'High' && (
              <div className="flex items-center gap-1 text-[11px] font-bold text-orange-500">
                <Flag className="w-3 h-3" />
                High
              </div>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

function TaskDetail({ onBack }: { onBack: () => void }) {
  return (
    <div className="flex flex-col min-h-[100dvh] bg-background text-foreground">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border px-4 py-3 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <Button onClick={onBack} variant="ghost" size="icon" className="h-10 w-10 -ml-2 text-muted-foreground rounded-full hover:bg-accent/50">
            <ArrowLeft className="w-6 h-6" />
          </Button>
          <span className="text-lg font-bold tracking-tight">Task Detail</span>
        </div>
        <Button variant="ghost" size="icon" className="h-10 w-10 text-red-500 rounded-full hover:bg-red-500/10">
          <Trash2 className="w-5 h-5" />
        </Button>
      </header>

      <main className="flex-1 p-5 space-y-6 overflow-y-auto pb-32">
        <textarea
          defaultValue="Submit weekly store performance report"
          className="w-full text-xl font-bold bg-transparent border-0 border-b border-border/0 hover:border-border/50 focus:border-primary p-0 resize-none focus:outline-none focus:ring-0 transition-colors h-16 leading-tight"
        />

        <section className="space-y-4">
          <div className="flex items-center gap-4 text-sm font-medium p-3 bg-card/50 rounded-xl border border-border/50">
            <div className="p-2 bg-primary/10 rounded-lg text-primary">
              <CalendarDays className="w-5 h-5" />
            </div>
            <div className="flex flex-col">
              <span className="text-muted-foreground text-xs uppercase tracking-widest">Due Date</span>
              <span>Today, 17:00</span>
            </div>
            <Button variant="ghost" size="icon" className="ml-auto w-8 h-8 rounded-full text-muted-foreground hover:bg-red-500/10 hover:text-red-500">
              <X className="w-4 h-4" />
            </Button>
          </div>

          <div className="flex items-center gap-4 text-sm font-medium p-3 bg-card/50 rounded-xl border border-border/50">
            <div className="p-2 bg-accent rounded-lg text-muted-foreground">
              <Bell className="w-5 h-5" />
            </div>
            <div className="flex flex-col">
              <span className="text-muted-foreground text-xs uppercase tracking-widest">Reminder</span>
              <span>At time of event</span>
            </div>
            <div className="ml-auto w-10 h-6 bg-primary rounded-full relative">
              <div className="absolute right-1 top-1 w-4 h-4 bg-background rounded-full" />
            </div>
          </div>
        </section>

        <section className="space-y-3 pt-2">
          <Label className="text-xs text-muted-foreground font-medium pl-1">Priority</Label>
          <div className="flex gap-2 w-full">
            <Button variant="outline" className="flex-1 h-12 rounded-xl text-xs font-bold border-border/60 hover:bg-accent/50 text-muted-foreground">Low</Button>
            <Button variant="outline" className="flex-1 h-12 rounded-xl text-xs font-bold border-orange-500/40 bg-orange-500/10 text-orange-500 hover:bg-orange-500/20 shadow-sm border-orange-500 max-w-[33%]">Medium</Button>
            <Button variant="outline" className="flex-1 h-12 rounded-xl text-xs font-bold border-red-500/40 text-red-500 hover:bg-red-500/10">High</Button>
          </div>
        </section>

        <section className="space-y-3 pt-2">
          <Label className="text-xs text-muted-foreground font-medium pl-1">Notes</Label>
          <textarea
            placeholder="Add additional details here..."
            defaultValue="Make sure to highlight the accessory sales bump."
            className="w-full h-32 p-4 bg-card/50 border border-border/80 text-base placeholder:text-muted-foreground/30 rounded-xl focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-primary transition-all resize-none"
          />
        </section>
      </main>

      <div className="fixed bottom-0 w-full p-4 bg-background/95 backdrop-blur border-t border-border z-50">
        <Button onClick={onBack} variant="outline" className="w-full h-14 text-[15px] font-bold tracking-wide rounded-xl border-border hover:bg-accent/50 group">
          <div className="w-5 h-5 rounded border-2 border-muted-foreground/40 mr-3 group-hover:border-foreground group-hover:bg-foreground transition-all" />
          Mark as Done
        </Button>
      </div>
    </div>
  );
}

// ------------------------------------------------------------------------------------------------
// TAB 4: SETTINGS
// ------------------------------------------------------------------------------------------------

function SettingsScreen({ onNavigate }: { onNavigate: (screen: string) => void }) {
  return (
    <div className="flex flex-col min-h-full">
      <header className="sticky top-0 z-40 bg-background/95 backdrop-blur border-b border-border py-4 px-4 flex flex-col gap-4">
        <div className="flex items-center justify-between">
          <span className="text-2xl font-bold tracking-tight">Settings</span>
        </div>
      </header>

      <div className="p-5 space-y-8">
        {/* User Card */}
        <div className="flex items-center gap-4 p-4 rounded-2xl bg-card border border-border/60">
          <div className="w-16 h-16 rounded-full bg-primary/10 border border-primary/20 flex items-center justify-center text-primary text-xl font-bold">
            JD
          </div>
          <div className="flex flex-col gap-1 flex-1">
            <span className="text-lg font-bold tracking-tight">John Doe</span>
            <span className="text-sm text-muted-foreground">Store 45 - Sales Agent</span>
          </div>
        </div>

        {/* Sales Group */}
        <section className="space-y-3">
          <h3 className="text-xs font-bold text-muted-foreground uppercase tracking-widest pl-1">Sales</h3>
          <div className="rounded-2xl bg-card border border-border/60 overflow-hidden">
            <SettingsRow
              icon={<Target className="w-5 h-5 text-primary" />}
              label="Edit Monthly Targets"
              onClick={() => onNavigate("edit-targets")}
            />
          </div>
        </section>

        {/* System Group */}
        <section className="space-y-3">
          <h3 className="text-xs font-bold text-muted-foreground uppercase tracking-widest pl-1">System</h3>
          <div className="rounded-2xl bg-card border border-border/60 overflow-hidden flex flex-col divide-y divide-border/50">
            <SettingsRow
              icon={<Bell className="w-5 h-5 text-muted-foreground" />}
              label="Notifications"
              rightElement={
                <div className="w-10 h-6 bg-primary rounded-full relative">
                  <div className="absolute right-1 top-1 w-4 h-4 bg-background rounded-full" />
                </div>
              }
            />
            <SettingsRow
              icon={<Moon className="w-5 h-5 text-muted-foreground" />}
              label="Dark Mode"
              rightElement={
                <div className="w-10 h-6 bg-primary rounded-full relative">
                  <div className="absolute right-1 top-1 w-4 h-4 bg-background rounded-full" />
                </div>
              }
            />
            <SettingsRow
              icon={<FileText className="w-5 h-5 text-muted-foreground" />}
              label="Terms & Privacy"
            />
            <SettingsRow
              icon={<HelpCircle className="w-5 h-5 text-muted-foreground" />}
              label="Help & Support"
            />
          </div>
        </section>

        {/* Destructive Action */}
        <section className="pt-4">
          <Button variant="outline" className="w-full h-14 rounded-xl border-red-500/30 text-red-500 hover:bg-red-500/10 font-bold tracking-wide flex gap-2">
            <LogOut className="w-5 h-5" />
            Log Out
          </Button>
        </section>
      </div>
    </div>
  );
}

function SettingsRow({ icon, label, onClick, rightElement }: any) {
  return (
    <div
      onClick={onClick}
      className={`flex items-center gap-4 p-4 hover:bg-accent/40 transition-colors ${onClick ? 'cursor-pointer' : ''}`}
    >
      <div className="p-2 bg-accent rounded-lg">
        {icon}
      </div>
      <span className="font-medium text-[15px] flex-1">{label}</span>
      {rightElement || <ChevronRightIcon className="w-5 h-5 text-muted-foreground/50" />}
    </div>
  );
}
