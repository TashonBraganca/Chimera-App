import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:flutter_animate/flutter_animate.dart';
import '../../providers/app_providers.dart';
import '../../widgets/glass_app_bar.dart';
import '../../../data/models/ranking_models.dart';

class ChatScreen extends ConsumerStatefulWidget {
  const ChatScreen({super.key});

  @override
  ConsumerState<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends ConsumerState<ChatScreen> {
  final TextEditingController _messageController = TextEditingController();
  final ScrollController _scrollController = ScrollController();
  final FocusNode _messageFocus = FocusNode();

  @override
  void dispose() {
    _messageController.dispose();
    _scrollController.dispose();
    _messageFocus.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;
    final chatState = ref.watch(chatStateProvider);

    return Scaffold(
      body: CustomScrollView(
        controller: _scrollController,
        slivers: [
          // Glass App Bar
          GlassAppBar(
            title: 'AI Investment Chat',
            backgroundColor: colorScheme.primary,
            leading: IconButton(
              icon: Icon(Icons.arrow_back_rounded, color: colorScheme.onPrimary),
              onPressed: () => context.pop(),
            ),
            actions: [
              if (chatState.messages.isNotEmpty)
                IconButton(
                  icon: Icon(Icons.clear_all_rounded, color: colorScheme.onPrimary),
                  onPressed: () => _showClearChatDialog(context),
                ),
            ],
          ),

          // Chat content
          if (chatState.messages.isEmpty)
            SliverFillRemaining(
              child: _buildWelcomeScreen(context, colorScheme),
            )
          else
            SliverPadding(
              padding: const EdgeInsets.all(16),
              sliver: SliverList(
                delegate: SliverChildBuilderDelegate(
                  (context, index) {
                    final message = chatState.messages[index];
                    return _buildMessageBubble(context, message, colorScheme)
                        .animate()
                        .fadeIn(duration: 400.ms)
                        .slideY(begin: 0.3, end: 0);
                  },
                  childCount: chatState.messages.length,
                ),
              ),
            ),

          // Loading indicator
          if (chatState.isLoading)
            SliverToBoxAdapter(
              child: _buildTypingIndicator(context, colorScheme)
                  .animate()
                  .fadeIn(duration: 300.ms),
            ),

          // Error message
          if (chatState.error != null)
            SliverToBoxAdapter(
              child: _buildErrorMessage(context, chatState.error!, colorScheme)
                  .animate()
                  .fadeIn(duration: 300.ms)
                  .shake(duration: 600.ms),
            ),

          // Bottom padding for input area
          const SliverToBoxAdapter(
            child: SizedBox(height: 80),
          ),
        ],
      ),
      bottomSheet: _buildMessageInput(context, colorScheme, chatState.isLoading),
    );
  }

  Widget _buildWelcomeScreen(BuildContext context, ColorScheme colorScheme) {
    final suggestions = [
      ('Why is Reliance ranked #1?', Icons.trending_up_rounded),
      ('Explain the scoring methodology', Icons.calculate_rounded),
      ('What factors affect rankings?', Icons.psychology_rounded),
      ('Compare TCS vs Infosys', Icons.compare_arrows_rounded),
    ];

    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          // Welcome header
          Container(
            padding: const EdgeInsets.all(32),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
                colors: [
                  colorScheme.primaryContainer.withOpacity(0.8),
                  colorScheme.secondaryContainer.withOpacity(0.6),
                ],
              ),
              borderRadius: BorderRadius.circular(24),
              boxShadow: [
                BoxShadow(
                  color: colorScheme.shadow.withOpacity(0.1),
                  blurRadius: 20,
                  offset: const Offset(0, 10),
                ),
              ],
            ),
            child: Column(
              children: [
                Icon(
                  Icons.psychology_rounded,
                  size: 64,
                  color: colorScheme.primary,
                )
                    .animate()
                    .scale(duration: 600.ms)
                    .then(delay: 200.ms)
                    .shimmer(duration: 2000.ms, color: colorScheme.primary.withOpacity(0.3)),
                const SizedBox(height: 20),
                Text(
                  'Ask Our AI Assistant',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                    fontWeight: FontWeight.bold,
                    color: colorScheme.onPrimaryContainer,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'Get detailed explanations about asset rankings, methodology, and investment insights.',
                  style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                    color: colorScheme.onPrimaryContainer.withOpacity(0.8),
                  ),
                  textAlign: TextAlign.center,
                ),
              ],
            ),
          ),

          const SizedBox(height: 32),

          // Suggestions
          Text(
            'Try asking:',
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
              fontWeight: FontWeight.w600,
              color: colorScheme.onSurface,
            ),
          ),

          const SizedBox(height: 16),

          ...suggestions.asMap().entries.map((entry) {
            final index = entry.key;
            final (suggestion, icon) = entry.value;
            return Container(
              margin: const EdgeInsets.only(bottom: 8),
              child: ListTile(
                leading: CircleAvatar(
                  backgroundColor: colorScheme.primaryContainer,
                  child: Icon(icon, color: colorScheme.primary),
                ),
                title: Text(suggestion),
                trailing: Icon(Icons.send_rounded, color: colorScheme.primary),
                onTap: () => _sendSuggestion(suggestion),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            )
                .animate()
                .fadeIn(duration: 600.ms, delay: (300 + index * 100).ms)
                .slideX(begin: 0.3, end: 0);
          }).toList(),
        ],
      ),
    );
  }

  Widget _buildMessageBubble(BuildContext context, ChatMessage message, ColorScheme colorScheme) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: message.isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        children: [
          if (!message.isUser) _buildAvatarIcon(colorScheme, false),
          if (!message.isUser) const SizedBox(width: 8),
          Flexible(
            child: Container(
              constraints: BoxConstraints(
                maxWidth: MediaQuery.of(context).size.width * 0.75,
              ),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: message.isUser
                    ? colorScheme.primary
                    : colorScheme.surfaceContainerHigh,
                borderRadius: BorderRadius.circular(16).copyWith(
                  topLeft: message.isUser ? const Radius.circular(16) : const Radius.circular(4),
                  topRight: message.isUser ? const Radius.circular(4) : const Radius.circular(16),
                ),
                boxShadow: [
                  BoxShadow(
                    color: colorScheme.shadow.withOpacity(0.1),
                    blurRadius: 8,
                    offset: const Offset(0, 2),
                  ),
                ],
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  if (message.isUser)
                    Text(
                      message.message,
                      style: TextStyle(
                        color: colorScheme.onPrimary,
                        fontSize: 16,
                      ),
                    )
                  else
                    _buildAIResponse(context, message, colorScheme),
                ],
              ),
            ),
          ),
          if (message.isUser) const SizedBox(width: 8),
          if (message.isUser) _buildAvatarIcon(colorScheme, true),
        ],
      ),
    );
  }

  Widget _buildAvatarIcon(ColorScheme colorScheme, bool isUser) {
    return CircleAvatar(
      radius: 16,
      backgroundColor: isUser ? colorScheme.primary : colorScheme.secondaryContainer,
      child: Icon(
        isUser ? Icons.person_rounded : Icons.smart_toy_rounded,
        size: 18,
        color: isUser ? colorScheme.onPrimary : colorScheme.onSecondaryContainer,
      ),
    );
  }

  Widget _buildAIResponse(BuildContext context, ChatMessage message, ColorScheme colorScheme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // Answer text
        if (message.answer != null)
          Text(
            message.answer!,
            style: TextStyle(
              color: colorScheme.onSurface,
              fontSize: 16,
              height: 1.5,
            ),
          ),

        // Confidence indicator
        if (message.confidence != null) ...[
          const SizedBox(height: 12),
          Row(
            children: [
              Icon(
                Icons.verified_outlined,
                size: 16,
                color: colorScheme.primary,
              ),
              const SizedBox(width: 6),
              Text(
                'Confidence: ${message.confidence}%',
                style: TextStyle(
                  color: colorScheme.onSurfaceVariant,
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: LinearProgressIndicator(
                  value: (message.confidence ?? 0) / 100,
                  backgroundColor: colorScheme.surfaceContainerHighest,
                  valueColor: AlwaysStoppedAnimation(colorScheme.primary),
                  minHeight: 2,
                ),
              ),
            ],
          ),
        ],

        // Citations
        if (message.citations != null && message.citations!.isNotEmpty) ...[
          const SizedBox(height: 16),
          Text(
            'Sources:',
            style: TextStyle(
              color: colorScheme.onSurfaceVariant,
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          ...message.citations!.map((citation) => _buildCitationChip(context, citation, colorScheme)),
        ],

        // Disclaimer
        if (message.disclaimer != null) ...[
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: colorScheme.surfaceContainerHighest.withOpacity(0.5),
              borderRadius: BorderRadius.circular(6),
              border: Border.all(
                color: colorScheme.outline.withOpacity(0.2),
              ),
            ),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(
                  Icons.info_outline_rounded,
                  size: 14,
                  color: colorScheme.onSurfaceVariant,
                ),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    message.disclaimer!,
                    style: TextStyle(
                      color: colorScheme.onSurfaceVariant,
                      fontSize: 11,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildCitationChip(BuildContext context, BackendCitation citation, ColorScheme colorScheme) {
    return Container(
      margin: const EdgeInsets.only(bottom: 4),
      child: Chip(
        avatar: Icon(
          Icons.source_rounded,
          size: 14,
          color: colorScheme.primary,
        ),
        label: Text(
          '${citation.source} - ${citation.title}',
          style: const TextStyle(fontSize: 11),
        ),
        backgroundColor: colorScheme.surfaceContainerHighest,
        materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
        visualDensity: VisualDensity.compact,
      ),
    );
  }

  Widget _buildTypingIndicator(BuildContext context, ColorScheme colorScheme) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          _buildAvatarIcon(colorScheme, false),
          const SizedBox(width: 8),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: colorScheme.surfaceContainerHigh,
              borderRadius: BorderRadius.circular(16).copyWith(
                topLeft: const Radius.circular(4),
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Text(
                  'AI is typing',
                  style: TextStyle(
                    color: colorScheme.onSurfaceVariant,
                    fontSize: 14,
                  ),
                ),
                const SizedBox(width: 8),
                SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    valueColor: AlwaysStoppedAnimation(colorScheme.primary),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorMessage(BuildContext context, String error, ColorScheme colorScheme) {
    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.errorContainer,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: colorScheme.error.withOpacity(0.2)),
      ),
      child: Row(
        children: [
          Icon(Icons.error_outline_rounded, color: colorScheme.error),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              error,
              style: TextStyle(color: colorScheme.onErrorContainer),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMessageInput(BuildContext context, ColorScheme colorScheme, bool isLoading) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: colorScheme.surface,
        border: Border(
          top: BorderSide(color: colorScheme.outline.withOpacity(0.2)),
        ),
      ),
      child: SafeArea(
        child: Row(
          children: [
            Expanded(
              child: TextField(
                controller: _messageController,
                focusNode: _messageFocus,
                decoration: InputDecoration(
                  hintText: 'Ask about investments...',
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(24),
                    borderSide: BorderSide.none,
                  ),
                  filled: true,
                  fillColor: colorScheme.surfaceContainerHigh,
                  contentPadding: const EdgeInsets.symmetric(
                    horizontal: 20,
                    vertical: 12,
                  ),
                ),
                maxLines: null,
                textInputAction: TextInputAction.send,
                onSubmitted: isLoading ? null : (_) => _sendMessage(),
              ),
            ),
            const SizedBox(width: 8),
            FloatingActionButton(
              onPressed: isLoading ? null : _sendMessage,
              backgroundColor: colorScheme.primary,
              foregroundColor: colorScheme.onPrimary,
              mini: true,
              child: isLoading
                  ? SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        valueColor: AlwaysStoppedAnimation(colorScheme.onPrimary),
                      ),
                    )
                  : const Icon(Icons.send_rounded),
            ),
          ],
        ),
      ),
    );
  }

  void _sendMessage() {
    final message = _messageController.text.trim();
    if (message.isEmpty) return;

    ref.read(chatStateProvider.notifier).sendMessage(message);
    _messageController.clear();
    _messageFocus.unfocus();

    // Scroll to bottom after a short delay
    Future.delayed(const Duration(milliseconds: 100), () {
      _scrollController.animateTo(
        _scrollController.position.maxScrollExtent,
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeOut,
      );
    });
  }

  void _sendSuggestion(String suggestion) {
    _messageController.text = suggestion;
    _sendMessage();
  }

  void _showClearChatDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Clear Chat History'),
        content: const Text('Are you sure you want to clear all chat messages?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              ref.read(chatStateProvider.notifier).clearMessages();
              Navigator.of(context).pop();
            },
            child: const Text('Clear'),
          ),
        ],
      ),
    );
  }
}