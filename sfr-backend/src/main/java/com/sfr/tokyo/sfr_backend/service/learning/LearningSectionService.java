package com.sfr.tokyo.sfr_backend.service.learning;

import com.sfr.tokyo.sfr_backend.dto.learning.LearningSectionDto;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningContent;
import com.sfr.tokyo.sfr_backend.entity.learning.LearningSection;
import com.sfr.tokyo.sfr_backend.exception.EntityNotFoundException;
import com.sfr.tokyo.sfr_backend.mapper.learning.LearningSectionMapper;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningContentRepository;
import com.sfr.tokyo.sfr_backend.repository.learning.LearningSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LearningSectionService {

    private final LearningSectionRepository learningSectionRepository;
    private final LearningContentRepository learningContentRepository;
    private final LearningSectionMapper learningSectionMapper;

    /**
     * セクションを作成
     * 
     * @param sectionDto セクションDTO
     * @return 作成されたセクションDTO
     */
    public LearningSectionDto createSection(LearningSectionDto sectionDto) {
        // コンテンツの存在確認
        LearningContent content = learningContentRepository.findById(sectionDto.getContentId())
                .orElseThrow(() -> new EntityNotFoundException("指定されたコンテンツが見つかりません: " + sectionDto.getContentId()));

        // セクション作成
        LearningSection section = LearningSection.builder()
                .contentId(sectionDto.getContentId())
                .title(sectionDto.getTitle())
                .displayOrder(sectionDto.getDisplayOrder())
                .build();

        LearningSection savedSection = learningSectionRepository.save(section);
        return learningSectionMapper.toDto(savedSection);
    }

    /**
     * コンテンツのセクション一覧を取得
     * 
     * @param contentId コンテンツID
     * @return セクションリスト
     */
    @Transactional(readOnly = true)
    public List<LearningSectionDto> getSectionsByContentId(Long contentId) {
        // コンテンツの存在確認
        if (!learningContentRepository.existsById(contentId)) {
            throw new EntityNotFoundException("指定されたコンテンツが見つかりません: " + contentId);
        }

        List<LearningSection> sections = learningSectionRepository.findByContentIdOrderByDisplayOrderAsc(contentId);
        return learningSectionMapper.toDtoList(sections);
    }

    /**
     * セクション詳細を取得
     * 
     * @param sectionId セクションID
     * @return セクションDTO
     */
    @Transactional(readOnly = true)
    public LearningSectionDto getSectionById(Long sectionId) {
        LearningSection section = learningSectionRepository.findByIdWithMaterials(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたセクションが見つかりません: " + sectionId));

        return learningSectionMapper.toDto(section);
    }

    /**
     * セクションを更新
     * 
     * @param sectionId  セクションID
     * @param sectionDto 更新DTO
     * @return 更新されたセクションDTO
     */
    public LearningSectionDto updateSection(Long sectionId, LearningSectionDto sectionDto) {
        LearningSection existingSection = learningSectionRepository.findById(sectionId)
                .orElseThrow(() -> new EntityNotFoundException("指定されたセクションが見つかりません: " + sectionId));

        // 更新可能フィールドのみ更新
        existingSection.setTitle(sectionDto.getTitle());
        existingSection.setDisplayOrder(sectionDto.getDisplayOrder());

        LearningSection updatedSection = learningSectionRepository.save(existingSection);
        return learningSectionMapper.toDto(updatedSection);
    }

    /**
     * セクションを削除
     * 
     * @param sectionId セクションID
     */
    public void deleteSection(Long sectionId) {
        if (!learningSectionRepository.existsById(sectionId)) {
            throw new EntityNotFoundException("指定されたセクションが見つかりません: " + sectionId);
        }

        learningSectionRepository.deleteById(sectionId);
    }
}
