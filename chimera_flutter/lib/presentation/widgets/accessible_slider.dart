import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class AccessibleSlider extends StatelessWidget {
  final double value;
  final double min;
  final double max;
  final int? divisions;
  final ValueChanged<double> onChanged;
  final String semanticLabel;
  final String Function(double) valueFormatter;

  const AccessibleSlider({
    super.key,
    required this.value,
    required this.min,
    required this.max,
    this.divisions,
    required this.onChanged,
    required this.semanticLabel,
    required this.valueFormatter,
  });

  @override
  Widget build(BuildContext context) {
    return Semantics(
      label: semanticLabel,
      value: valueFormatter(value),
      increasedValue: value < max ? valueFormatter(_getNextValue()) : null,
      decreasedValue: value > min ? valueFormatter(_getPreviousValue()) : null,
      onIncrease: value < max ? () => _adjustValue(true) : null,
      onDecrease: value > min ? () => _adjustValue(false) : null,
      slider: true,
      child: Slider(
        value: value,
        min: min,
        max: max,
        divisions: divisions,
        label: valueFormatter(value),
        onChanged: onChanged,
        semanticFormatterCallback: (value) => valueFormatter(value),
      ),
    );
  }

  double _getNextValue() {
    if (divisions != null) {
      final step = (max - min) / divisions!;
      return (value + step).clamp(min, max);
    }
    return (value + (max - min) * 0.01).clamp(min, max);
  }

  double _getPreviousValue() {
    if (divisions != null) {
      final step = (max - min) / divisions!;
      return (value - step).clamp(min, max);
    }
    return (value - (max - min) * 0.01).clamp(min, max);
  }

  void _adjustValue(bool increase) {
    final newValue = increase ? _getNextValue() : _getPreviousValue();
    onChanged(newValue);
  }
}

class AccessibleAmountSlider extends StatelessWidget {
  final double amount;
  final ValueChanged<double> onChanged;

  const AccessibleAmountSlider({
    super.key,
    required this.amount,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final currencyFormat = NumberFormat.currency(
      locale: 'en_IN',
      symbol: '₹',
      decimalDigits: 0,
    );

    return AccessibleSlider(
      value: amount,
      min: 10000,
      max: 10000000,
      divisions: 100,
      onChanged: onChanged,
      semanticLabel: 'Investment amount slider',
      valueFormatter: (value) => currencyFormat.format(value),
    );
  }
}