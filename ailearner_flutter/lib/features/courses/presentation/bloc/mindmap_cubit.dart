import 'package:equatable/equatable.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import '../../data/course_repository.dart';

class MindMapNode extends Equatable {
  const MindMapNode({
    required this.title,
    this.children = const [],
  });

  final String title;
  final List<MindMapNode> children;

  @override
  List<Object?> get props => [title, children];
}

class MindMapState extends Equatable {
  const MindMapState({
    this.loading = false,
    this.nodes = const [],
    this.error,
  });

  final bool loading;
  final List<MindMapNode> nodes;
  final String? error;

  MindMapState copyWith({
    bool? loading,
    List<MindMapNode>? nodes,
    String? error,
  }) {
    return MindMapState(
      loading: loading ?? this.loading,
      nodes: nodes ?? this.nodes,
      error: error,
    );
  }

  @override
  List<Object?> get props => [loading, nodes, error];
}

class MindMapCubit extends Cubit<MindMapState> {
  MindMapCubit(this._repository) : super(const MindMapState());

  final CourseRepository _repository;

  Future<void> loadCourse(String courseId) async {
    emit(state.copyWith(loading: true, error: null));
    try {
      final full = await _repository.getFullCourse(courseId);
      final units = (full['units'] as List?) ?? <dynamic>[];
      final nodes = units.whereType<Map>().map((u) {
        final unit = u.cast<String, dynamic>();
        final subtopics = (unit['subtopics'] as List?) ?? <dynamic>[];
        return MindMapNode(
          title: unit['title'] as String? ?? 'Untitled unit',
          children: subtopics.whereType<Map>().map((s) {
            final sub = s.cast<String, dynamic>();
            return MindMapNode(title: sub['title'] as String? ?? 'Untitled subtopic');
          }).toList(),
        );
      }).toList();
      emit(state.copyWith(loading: false, nodes: nodes));
    } catch (e) {
      emit(state.copyWith(loading: false, error: e.toString()));
    }
  }
}
