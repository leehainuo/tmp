import {
  LayoutDashboard,
  Monitor,
  Bug,
  ListTodo,
  FileX,
  HelpCircle,
  Lock,
  Bell,
  Package,
  Palette,
  ServerOff,
  Settings,
  Wrench,
  UserCog,
  UserX,
  Users,
  MessagesSquare,
  ShieldCheck,
  AudioWaveform,
  Command,
  GalleryVerticalEnd,
  Construction,
  Home,
  Folder,
  FileText,
  Database,
  BarChart3,
  PieChart,
  LineChart,
  TrendingUp,
  ShoppingCart,
  CreditCard,
  Mail,
  Phone,
  MapPin,
  Calendar,
  Clock,
  Search,
  Filter,
  Download,
  Upload,
  Edit,
  Trash2,
  Plus,
  Minus,
  X,
  Check,
  ChevronRight,
  ChevronDown,
  ChevronUp,
  ChevronLeft,
  ArrowRight,
  ArrowLeft,
  ArrowUp,
  ArrowDown,
  MoreVertical,
  Eye,
  EyeOff,
  Key,
  LogOut,
  LogIn,
  User,
  UserPlus,
  UserMinus,
  Star,
  Heart,
  Bookmark,
  Tag,
  Image,
  Video,
  Music,
  Film,
  Camera,
  ImagePlus,
  File,
  Archive,
  Inbox,
  Send,
  Paperclip,
  Link,
  ExternalLink,
  Copy,
  Share,
  Save,
  RefreshCw,
  RotateCw,
  Power,
  Zap,
  Sun,
  Moon,
  Globe,
  Languages,
  Info,
  AlertCircle,
  AlertTriangle,
  CheckCircle,
  XCircle,
  Loader,
  type LucideIcon,
  Menu,
  Columns3Cog,
  UserRoundCog,
  FolderCog,
  FileCog,
} from 'lucide-react'

// 图标名称到图标的映射
const iconMap: Record<string, LucideIcon> = {
  // 基础图标
  home: Home,
  dashboard: LayoutDashboard,
  folder: Folder,
  file: File,
  fileCog: FileCog,
  fileText: FileText,
  database: Database,
  
  // 图表相关
  barChart: BarChart3,
  pieChart: PieChart,
  lineChart: LineChart,
  trendingUp: TrendingUp,
  
  // 用户相关
  user: User,
  users: Users,
  userPlus: UserPlus,
  userMinus: UserMinus,
  userCog: UserCog,
  userRoundCog: UserRoundCog,
  userX: UserX,
  
  // 权限相关
  menu: Menu,
  columnsCog: Columns3Cog,

  // 部门相关
  folderCog: FolderCog,

  // 系统相关
  settings: Settings,
  monitor: Monitor,
  server: ServerOff,
  shield: ShieldCheck,
  lock: Lock,
  key: Key,
  power: Power,
  zap: Zap,
  
  // 操作相关
  edit: Edit,
  delete: Trash2,
  plus: Plus,
  minus: Minus,
  check: Check,
  x: X,
  search: Search,
  filter: Filter,
  download: Download,
  upload: Upload,
  copy: Copy,
  share: Share,
  save: Save,
  refresh: RefreshCw,
  rotate: RotateCw,
  
  // 导航相关
  chevronRight: ChevronRight,
  chevronDown: ChevronDown,
  chevronUp: ChevronUp,
  chevronLeft: ChevronLeft,
  arrowRight: ArrowRight,
  arrowLeft: ArrowLeft,
  arrowUp: ArrowUp,
  arrowDown: ArrowDown,
  moreVertical: MoreVertical,
  
  // 内容相关
  image: Image,
  video: Video,
  music: Music,
  film: Film,
  camera: Camera,
  imagePlus: ImagePlus,
  archive: Archive,
  inbox: Inbox,
  send: Send,
  paperclip: Paperclip,
  link: Link,
  externalLink: ExternalLink,
  
  // 状态相关
  eye: Eye,
  eyeOff: EyeOff,
  star: Star,
  heart: Heart,
  bookmark: Bookmark,
  tag: Tag,
  
  // 认证相关
  logIn: LogIn,
  logOut: LogOut,
  
  // 主题相关
  sun: Sun,
  moon: Moon,
  globe: Globe,
  languages: Languages,
  
  // 通知相关
  bell: Bell,
  mail: Mail,
  phone: Phone,
  mapPin: MapPin,
  calendar: Calendar,
  clock: Clock,
  
  // 信息相关
  info: Info,
  alertCircle: AlertCircle,
  alertTriangle: AlertTriangle,
  checkCircle: CheckCircle,
  xCircle: XCircle,
  loader: Loader,
  helpCircle: HelpCircle,
  
  // 业务相关
  shoppingCart: ShoppingCart,
  creditCard: CreditCard,
  package: Package,
  messagesSquare: MessagesSquare,
  listTodo: ListTodo,
  bug: Bug,
  fileX: FileX,
  construction: Construction,
  palette: Palette,
  wrench: Wrench,
  audioWaveform: AudioWaveform,
  command: Command,
  galleryVerticalEnd: GalleryVerticalEnd,
}

/**
 * 根据图标名称获取对应的 Lucide 图标组件
 * @param iconName 图标名称（不区分大小写，支持驼峰命名）
 * @returns Lucide 图标组件，如果找不到则返回默认图标
 */
export function getIcon(iconName?: string | null): LucideIcon | undefined {
  if (!iconName) {
    return undefined
  }
  
  // 移除空格和特殊字符，保留字母和数字
  const cleaned = iconName.trim().replace(/[^a-zA-Z0-9]/g, '')
  
  // 1. 先尝试直接匹配（保持原始大小写）
  if (iconMap[cleaned]) {
    return iconMap[cleaned]
  }
  
  // 2. 尝试小写匹配（兼容小写输入）
  const lowerName = cleaned.toLowerCase()
  if (iconMap[lowerName]) {
    return iconMap[lowerName]
  }
  
  // 3. 尝试驼峰转小写匹配（userCog -> usercog）
  // 先找到所有可能的匹配键
  const possibleKeys = Object.keys(iconMap).filter(key => 
    key.toLowerCase() === lowerName
  )
  if (possibleKeys.length > 0) {
    return iconMap[possibleKeys[0]]
  }
  
  // 4. 最后尝试模糊匹配（包含关系）
  const matchedKey = Object.keys(iconMap).find(key => {
    const keyLower = key.toLowerCase()
    return keyLower.includes(lowerName) || lowerName.includes(keyLower)
  })
  
  if (matchedKey) {
    return iconMap[matchedKey]
  }
  
  // 如果都找不到，返回 undefined（前端可以显示默认图标或文本）
  return undefined
}

