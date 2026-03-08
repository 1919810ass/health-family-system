package com.healthfamily.service;

import com.healthfamily.domain.constant.ConstitutionType;
import com.healthfamily.domain.entity.AssessmentOption;
import com.healthfamily.domain.entity.AssessmentQuestion;
import com.healthfamily.domain.entity.AssessmentQuestionnaire;
import com.healthfamily.domain.entity.Constitution;
import com.healthfamily.domain.repository.AssessmentQuestionnaireRepository;
import com.healthfamily.domain.repository.ConstitutionRepository;
import com.healthfamily.domain.repository.HealthReminderTemplateRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.healthfamily.domain.entity.HealthReminderTemplate;

@Service
@RequiredArgsConstructor
public class DataInitializationService {

    private final ConstitutionRepository constitutionRepository;
    private final AssessmentQuestionnaireRepository questionnaireRepository;
    private final HealthReminderTemplateRepository reminderTemplateRepository;

    @PostConstruct
    @Transactional
    public void initializeData() {
        if (constitutionRepository.count() > 0) {
            return; // Data already exists
        }

        createConstitutions();
        createDefaultQuestionnaire();
        createDefaultReminderTemplates();
    }

    private void createConstitutions() {
        List<Constitution> constitutions = Arrays.asList(
            createConstitution(ConstitutionType.PING_HE, "平和质", "阴阳气血调和，体态适中、面色红润、精力充沛。", "先天禀赋良好，后天调养得当。", "体形匀称健壮，面色、肤色润泽，头发稠密有光泽，目光有神，唇色红润，不易疲劳，精力充沛，睡眠、食欲好，大小便正常，性格随和开朗。", "饮食均衡，不宜过饥过饱、过冷过热。", "适度锻炼，如散步、慢跑、太极拳。", "起居有常，劳逸结合，保持乐观情绪。"),
            createConstitution(ConstitutionType.QI_XU, "气虚质", "元气不足，以疲乏、气短、自汗等气虚表现为主要特征。", "先天不足，后天失养，或久病未愈。", "语声低怯，气短懒言，精神不振，易疲劳，易出汗，舌淡红，舌边有齿痕。", "宜食益气健脾的食物，如小米、山药、大枣。", "宜做柔缓运动，如散步、太极拳，不宜剧烈运动。", "注意保暖，避免劳累，保持稳定情绪。"),
            createConstitution(ConstitutionType.YANG_XU, "阳虚质", "阳气不足，以畏寒怕冷、手足不温等虚寒表现为主要特征。", "先天阳气不足，或久病伤阳，或过食寒凉。", "畏寒肢冷，面色㿠白，口淡不渴，喜热饮，小便清长，大便溏薄。", "宜食温补阳气的食物，如羊肉、韭菜、生姜。", "宜在温暖环境中进行和缓运动，如慢跑、快走。", "注意保暖，尤其腰背和足部，避免熬夜。"),
            createConstitution(ConstitutionType.YIN_XU, "阴虚质", "阴液亏少，以口燥咽干、手足心热等虚热表现为主要特征。", "先天阴液不足，或热病伤阴，或过食辛辣。", "体形偏瘦，手足心热，口燥咽干，鼻微干，喜冷饮，大便干燥，舌红少津。", "宜食滋阴润燥的食物，如银耳、百合、梨。", "宜做中小强度、间断性的锻炼，避免大汗淋漓。", "避免熬夜，节制房事，保持心境平和。"),
            createConstitution(ConstitutionType.TAN_SHI, "痰湿质", "痰湿凝聚，以体形肥胖、腹部肥满、口黏苔腻等痰湿表现为主要特征。", "过食肥甘，或外感湿邪，或脾运不健。", "体形肥胖，腹部肥满松软，面部皮肤油脂较多，多汗且黏，胸闷，痰多。", "饮食宜清淡，少食肥甘厚味，可多食海带、冬瓜。", "宜做中等强度的有氧运动，如长跑、游泳。", "衣着宜透气，居室保持干燥，避免潮湿环境。"),
            createConstitution(ConstitutionType.SHI_RE, "湿热质", "湿热内蕴，以面垢油光、口苦、苔黄腻等湿热表现为主要特征。", "素体阳盛，或过食辛辣，或外感湿热。", "面垢油光，易生痤疮，口苦口干，身重困倦，大便黏滞不畅，小便短黄。", "饮食宜清淡，多食清热利湿的食物，如绿豆、薏苡仁、苦瓜。", "宜做中等强度的运动，如跑步、游泳、爬山。", "避免熬夜，戒烟限酒，保持大便通畅。"),
            createConstitution(ConstitutionType.XUE_YU, "血瘀质", "血行不畅，以肤色晦黯、舌质紫黯等血瘀表现为主要特征。", "情志不遂，或久病入络，或外伤血瘀。", "面色晦黯，皮肤偏黯或色素沉着，易出现瘀斑，口唇黯淡，舌质紫黯。", "宜食活血化瘀的食物，如山楂、黑木耳、醋。", "宜做有助于促进气血运行的运动，如太极拳、舞蹈。", "保持乐观情绪，避免长时间保持同一姿势。"),
            createConstitution(ConstitutionType.QI_YU, "气郁质", "气机郁滞，以神情抑郁、忧虑脆弱等气郁表现为主要特征。", "长期情志不遂，或突发精神刺激。", "神情抑郁，情感脆弱，烦闷不乐，易紧张、焦虑，多愁善感，胸胁胀痛。", "宜食疏肝理气的食物，如黄花菜、玫瑰花、柑橘。", "宜参加集体性运动，如乒乓球、羽毛球、旅游。", "多参加社交活动，倾诉心声，开阔心胸。"),
            createConstitution(ConstitutionType.TE_BING, "特禀质", "先天失常，以生理缺陷、过敏反应等为主要特征。", "先天遗传，或环境因素，或药物影响。", "多为遗传性疾病或过敏体质，如过敏性鼻炎、哮喘、皮肤易过敏。", "饮食宜清淡、均衡，忌食致敏食物。", "根据身体情况适度锻炼，避免接触过敏原。", "了解自身过敏原，避免接触，保持室内清洁。"));
        constitutionRepository.saveAll(constitutions);
    }

    private void createDefaultQuestionnaire() {
        AssessmentQuestionnaire questionnaire = new AssessmentQuestionnaire();
        questionnaire.setTitle("中医体质九分法问卷(简化版)");
        questionnaire.setDescription("本问卷用于评估您的中医体质类型，请根据近一年的体验和感觉，回答所有问题。");
        questionnaire.setActive(true);

        List<AssessmentQuestion> questions = new ArrayList<>();
        Map<ConstitutionType, List<String>> questionMap = getQuestionTexts();

        int order = 1;
        for (Map.Entry<ConstitutionType, List<String>> entry : questionMap.entrySet()) {
            ConstitutionType type = entry.getKey();
            for (String text : entry.getValue()) {
                AssessmentQuestion question = new AssessmentQuestion();
                question.setText(text);
                question.setConstitutionType(type);
                question.setDisplayOrder(order++);
                question.setQuestionnaire(questionnaire);
                question.setOptions(createStandardOptions(question));
                questions.add(question);
            }
        }
        questionnaire.setQuestions(questions);
        questionnaireRepository.save(questionnaire);
    }

    private List<AssessmentOption> createStandardOptions(AssessmentQuestion question) {
        List<AssessmentOption> options = new ArrayList<>();
        options.add(createOption("没有", 1, question));
        options.add(createOption("很少", 2, question));
        options.add(createOption("有时", 3, question));
        options.add(createOption("经常", 4, question));
        options.add(createOption("总是", 5, question));
        return options;
    }

    private AssessmentOption createOption(String text, int score, AssessmentQuestion question) {
        AssessmentOption option = new AssessmentOption();
        option.setText(text);
        option.setScore(score);
        option.setQuestion(question);
        return option;
    }

    private Constitution createConstitution(ConstitutionType type, String name, String description, String cause, String performance, String diet, String sport, String lifestyle) {
        Constitution c = new Constitution();
        c.setType(type);
        c.setName(name);
        c.setDescription(description);
        c.setCause(cause);
        c.setPerformance(performance);
        c.setDietAdvice(diet);
        c.setSportAdvice(sport);
        c.setLifestyleAdvice(lifestyle);
        return c;
    }

    private Map<ConstitutionType, List<String>> getQuestionTexts() {
        return Map.of(
            ConstitutionType.QI_XU, Arrays.asList(
                "(您)容易疲乏吗?",
                "(您)说话声音低弱无力吗?"
            ),
            ConstitutionType.YANG_XU, Arrays.asList(
                "(您)手脚发凉吗?",
                "(您)胃脘部、背部或腰膝部怕冷吗?"
            ),
            ConstitutionType.YIN_XU, Arrays.asList(
                "(您)感到手脚心发热吗?",
                "(您)口燥咽干，总想喝水吗?"
            ),
            ConstitutionType.TAN_SHI, Arrays.asList(
                "(您)感到胸闷或腹部胀满吗?",
                "(您)感到身体沉重不轻松或不爽快吗?"
            ),
            ConstitutionType.SHI_RE, Arrays.asList(
                "(您)面部或鼻部有油腻感或者油亮发光吗?",
                "(您)容易生痤疮或疮疖吗?"
            ),
            ConstitutionType.XUE_YU, Arrays.asList(
                "(您)皮肤在不知不觉中会出现青紫瘀斑吗?",
                "(您)两颧部有细微的红丝吗?"
            ),
            ConstitutionType.QI_YU, Arrays.asList(
                "(您)感到闷闷不乐、情绪低沉吗?",
                "(您)感到精神紧张、焦虑不安吗?"
            ),
            ConstitutionType.TE_BING, Arrays.asList(
                "(您)没有感冒时也会打喷嚏、流鼻涕吗?",
                "(您)因季节变化、温度变化或异味等原因而咳喘吗?"
            )
        );
    }

    private void createDefaultReminderTemplates() {
        if (reminderTemplateRepository.count() > 0) {
            return;
        }
        List<HealthReminderTemplate> templates = Arrays.asList(
            createReminderTemplate("请记得在饭后半小时服用降压药", "用药提醒", 1),
            createReminderTemplate("每日进行30分钟有氧运动", "运动提醒", 1),
            createReminderTemplate("晚餐请选择低盐低脂食物", "饮食提醒", 1),
            createReminderTemplate("晚上11点前准备入睡", "作息提醒", 0)
        );
        reminderTemplateRepository.saveAll(templates);
    }

    private HealthReminderTemplate createReminderTemplate(String content, String category, int status) {
        HealthReminderTemplate template = new HealthReminderTemplate();
        template.setContent(content);
        template.setCategory(category);
        template.setStatus(status);
        template.setUserCount((int) (Math.random() * 500)); // Add some random user count
        return template;
    }
}
