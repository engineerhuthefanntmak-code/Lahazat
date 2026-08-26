import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'glassmorphic_card.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class AppColors {
  static const Color ivory = Color(0xFFF4EFE6);
  static const Color deepEmerald = Color(0xFF1E4D3B);
  static const Color gold = Color(0xFFC9A227);
  static const Color coral = Color(0xFFD96C4F);
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'الريّاش',
      debugShowCheckedModeBanner: false,
      locale: const Locale('ar', ''),
      supportedLocales: const [Locale('ar', '')],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: ThemeData(
        scaffoldBackgroundColor: AppColors.ivory,
        fontFamily: 'Amiri',
        colorScheme: ColorScheme.fromSeed(
          seedColor: AppColors.deepEmerald,
          primary: AppColors.deepEmerald,
          secondary: AppColors.gold,
          surface: AppColors.ivory,
          error: AppColors.coral,
        ),
      ),
      builder: (context, child) {
        return Directionality(
          textDirection: TextDirection.rtl,
          child: child!,
        );
      },
      home: const MainImmersiveScreen(),
    );
  }
}

class MainImmersiveScreen extends StatefulWidget {
  const MainImmersiveScreen({super.key});

  @override
  State<MainImmersiveScreen> createState() => _MainImmersiveScreenState();
}

class _MainImmersiveScreenState extends State<MainImmersiveScreen> {
  bool _isImmersive = true;

  @override
  void initState() {
    super.initState();
    _applyImmersiveMode(_isImmersive);
  }

  void _applyImmersiveMode(bool enable) {
    if (enable) {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
    } else {
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.edgeToEdge);
    }
  }

  void _toggleImmersiveMode() {
    setState(() {
      _isImmersive = !_isImmersive;
      _applyImmersiveMode(_isImmersive);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.ivory,
      body: Stack(
        children: [
          // Background decorative elements
          Positioned(
            top: -50,
            right: -50,
            child: Container(
              width: 220,
              height: 220,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: AppColors.deepEmerald.withValues(alpha: 0.15),
              ),
            ),
          ),
          Positioned(
            bottom: -60,
            left: -40,
            child: Container(
              width: 250,
              height: 250,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: AppColors.coral.withValues(alpha: 0.15),
              ),
            ),
          ),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(24.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  const SizedBox(height: 12),
                  // Header using Diwani / Thuluth font
                  const Text(
                    'الريّاش',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontFamily: 'DiwaniThuluth',
                      fontSize: 48,
                      fontWeight: FontWeight.bold,
                      color: AppColors.deepEmerald,
                      height: 1.2,
                    ),
                  ),
                  const SizedBox(height: 8),
                  const Text(
                    'في رواية شعبة بن عياش',
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontFamily: 'Amiri',
                      fontSize: 18,
                      color: AppColors.coral,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 32),

                  // Reusable Glassmorphism component showcase
                  GlassmorphicCard(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            const Text(
                              'بطاقة زجاجية مخصصة',
                              style: TextStyle(
                                fontFamily: 'DiwaniThuluth',
                                fontSize: 24,
                                color: AppColors.deepEmerald,
                              ),
                            ),
                            Container(
                              padding: const EdgeInsets.symmetric(
                                  horizontal: 10, vertical: 4),
                              decoration: BoxDecoration(
                                color: AppColors.gold,
                                borderRadius: BorderRadius.circular(12),
                              ),
                              child: const Text(
                                'تطبيقي',
                                style: TextStyle(
                                  fontFamily: 'Amiri',
                                  fontSize: 14,
                                  color: Colors.white,
                                  fontWeight: FontWeight.bold,
                                ),
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 16),
                        const Text(
                          'هذه البطاقة مصممة بخصائص Glassmorphism المتقدمة: شفوفية ٤٥٪، ضبابية خلفية ٢٠ بكسل، إطار ذهبي رفيع، وظلال نيومورفيك مزدوجة.',
                          style: TextStyle(
                            fontFamily: 'Amiri',
                            fontSize: 16,
                            color: Colors.black87,
                            height: 1.6,
                          ),
                        ),
                        const SizedBox(height: 20),
                        ElevatedButton.icon(
                          onPressed: _toggleImmersiveMode,
                          icon: Icon(
                            _isImmersive
                                ? Icons.fullscreen_exit
                                : Icons.fullscreen,
                            color: Colors.white,
                          ),
                          label: Text(
                            _isImmersive
                                ? 'إيقاف الشاشة الكاملة'
                                : 'تفعيل الشاشة الكاملة',
                            style: const TextStyle(
                              fontFamily: 'Amiri',
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                              color: Colors.white,
                            ),
                          ),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: AppColors.deepEmerald,
                            padding: const EdgeInsets.symmetric(
                              horizontal: 20,
                              vertical: 12,
                            ),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                          ),
                        ),
                      ],
                    ),
                  ),

                  const Spacer(),

                  // Bottom Status Indicator Card
                  GlassmorphicCard(
                    padding: const EdgeInsets.all(16),
                    borderRadius: BorderRadius.circular(16),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'وضع الشاشة الكاملة:',
                          style: TextStyle(
                            fontFamily: 'Amiri',
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                            color: AppColors.deepEmerald,
                          ),
                        ),
                        Text(
                          _isImmersive ? 'مفعل (Immersive Mode)' : 'معطل',
                          style: TextStyle(
                            fontFamily: 'Amiri',
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                            color: _isImmersive
                                ? AppColors.deepEmerald
                                : AppColors.coral,
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}
