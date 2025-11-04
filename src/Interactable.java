public interface Interactable {
    void onInteractionStart(Player player);
    void onInteractionUpdate(Player player, double deltaTime);
    void onInteractionEnd(Player player);
    boolean canInteract(Player player);
    String getInteractionPrompt();
}
