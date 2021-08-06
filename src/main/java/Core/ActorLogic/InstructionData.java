package Core.ActorLogic;

class InstructionData
{
    String possibleValue;
    String dialogueFile;
    String dialogueId;

    public InstructionData(String possibleValue, String dialogueFile, String dialogueId)
    {
        this.possibleValue = possibleValue;
        this.dialogueFile = dialogueFile;
        this.dialogueId = dialogueId;
    }

    @Override
    public String toString()
    {
        return possibleValue + "---" + dialogueFile + " " + dialogueId;
    }
}
