package com.codecool.jlamas.controllers;

import com.codecool.jlamas.database.DoneQuestDAO;
import com.codecool.jlamas.database.StudentDAO;
import com.codecool.jlamas.models.account.Student;
import com.codecool.jlamas.models.quest.Quest;
import com.codecool.jlamas.database.QuestDAO;
import com.codecool.jlamas.views.QuestView;

import java.util.ArrayList;

public class QuestController {
    private QuestDAO questDAO;
    private QuestView view;
    private DoneQuestDAO doneQuestDAO;
    private StudentDAO studentDAO;

    public QuestController() {
        this.questDAO = new QuestDAO();
        this.view = new QuestView();
        this.doneQuestDAO = new DoneQuestDAO();
        this.studentDAO = new StudentDAO();
    }

    public void editQuest(String oldName, Quest quest) {
        questDAO.updateQuest(quest, oldName);

    }

    public void createQuest(Quest quest) {

        this.questDAO.insertQuest(quest);

    }

    public void deleteQuest(Quest quest) {
        questDAO.deleteQuest(quest);
    }

    public void markQuestAsDone(Student student, Quest quest) {
        doneQuestDAO.insert(student, quest);
        student.getWallet().put(quest.getReward());
        studentDAO.update(student);
    }

    public ArrayList<Quest> showAllQuests() {
        ArrayList<Quest> questsList = new ArrayList<>();
        questsList = this.questDAO.selectAll();

        return questsList;
    }

    public Quest chooseQuest(String questName) {
        Quest quest = questDAO.selectQuest(questName);
        return quest;
    }
}
