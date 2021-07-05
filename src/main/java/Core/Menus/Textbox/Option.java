package Core.Menus.Textbox;

class Option
{
    String optionMessage;
    OptionConditionData conditionData;
    private String nextDialogue;

    Option(String optionMessage, String nextDialogue)
    {
        this.optionMessage = optionMessage;
        this.nextDialogue = nextDialogue;
    }

    Option(String optionMessage, String nextDialogue, OptionConditionData conditionData)
    {
        this.optionMessage = optionMessage;
        this.nextDialogue = nextDialogue;
        this.conditionData = conditionData;
    }

    public String getNextDialogueEvaluated()
    {
        if (conditionData.conditionType.equals("NONE"))
            return nextDialogue;
        else
            return conditionData.getEvaluatedResult();
    }

    @Override
    public String toString()
    {
        return "Option{" +
                "nextDialogue='" + nextDialogue + '\'' +
                ", optionMessage='" + optionMessage + '\'' +
                '}';
    }

}
