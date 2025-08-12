import 'package:flutter/material.dart';
import 'dart:ui';

class GlassAppBar extends StatelessWidget {
  final String title;
  final Color? backgroundColor;
  final List<Widget>? actions;
  final bool centerTitle;
  final Widget? leading;

  const GlassAppBar({
    super.key,
    required this.title,
    this.backgroundColor,
    this.actions,
    this.centerTitle = true,
    this.leading,
  });

  @override
  Widget build(BuildContext context) {
    final colorScheme = Theme.of(context).colorScheme;
    final bgColor = backgroundColor ?? colorScheme.primary;

    return SliverAppBar(
      expandedHeight: 120,
      floating: true,
      pinned: true,
      elevation: 0,
      surfaceTintColor: Colors.transparent,
      backgroundColor: Colors.transparent,
      leading: leading,
      actions: actions,
      centerTitle: centerTitle,
      flexibleSpace: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              bgColor,
              bgColor.withOpacity(0.8),
            ],
          ),
        ),
        child: ClipRRect(
          child: BackdropFilter(
            filter: ImageFilter.blur(sigmaX: 10, sigmaY: 10),
            child: Container(
              decoration: BoxDecoration(
                color: bgColor.withOpacity(0.1),
              ),
            ),
          ),
        ),
      ),
      title: Text(
        title,
        style: TextStyle(
          color: colorScheme.onPrimary,
          fontWeight: FontWeight.w600,
          fontSize: 20,
        ),
      ),
    );
  }
}