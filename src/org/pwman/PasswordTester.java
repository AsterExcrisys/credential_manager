package pws.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class PasswordTester {

    private static final HashMap<String, String> requirements = new HashMap<>(0);
    private static final HashMap<String, String> advantages = new HashMap<>(0);
    private static final HashMap<String, String> disadvantages = new HashMap<>(0);
    private final String password;

    static {
        requirements.put("^[a-zA-Z0-9\\-+.*^$;,!£%&=@#_<>]+$", "Must contain at least one valid character");
        advantages.put("^(.*[a-z].*){5,}$", "Should have five or more lower-case letters");
        advantages.put("^(.*[A-Z].*)+$", "Should have one or more upper-case letters");
        advantages.put("^(.*[0-9].*){3,}$", "Should have three or more decimal numbers");
        advantages.put("^(.*[\\-+.*^$;,!£%&=@#_<>].*){1,}$", "Should have one or more special characters");
        advantages.put("^.{12,}$", "Should have twelve or more total characters");
        disadvantages.put("^.*[a-zA-Z]{8,}.*$", "Should not have eight or more consecutive lower/upper-case letters");
        disadvantages.put("^.*[0-9]{6,}.*$", "Should not have six or more consecutive decimal numbers");
        disadvantages.put("^.*[\\-+.*^$;,!£%&=@#_<>]{4,}.*$", "Should not have four or more consecutive special characters");
        disadvantages.put("^.{1,6}$", "Should not have six or less total characters");
    }
    
    public PasswordTester(String password) throws NullPointerException {
        this.password = Objects.requireNonNull(password);
    }

    public boolean isPasswordValid() {
        for (String requirement : requirements.keySet()) {
            if (!Pattern.matches(requirement, this.password)) {
                return false;
            }
        }
        return true;
    }

    public float getStrengthScore() {
        if (!this.isPasswordValid()) {
            return 0F;
        }
        float score = 0F;
        for (String advantage : advantages.keySet()) {
            if (Pattern.matches(advantage, this.password)) {
                score += 100F / advantages.size();
            }
        }
        for (String disadvantage : disadvantages.keySet()) {
            if (Pattern.matches(disadvantage, this.password)) {
                score -= 100F / disadvantages.size();
            }
        }
        return score <= 100F? Math.max(score, 0F):100F;
    }

    public String getStrengthGrade() {
        float score = this.getStrengthScore();
        if (score >= 80F && score <= 100F) {
            return "Excellent";
        } else if (score >= 60F && score <= 80F) {
            return "Good";
        } else if (score >= 40F && score <= 60F) {
            return "Decent";
        } else if (score >= 20F && score <= 40F) {
            return "Bad";
        } else {
            return "Terrible";
        }
    }

    public String[] getSafetyAdvices() {
        ArrayList<String> advices = new ArrayList<>(0);
        for (String advantage : advantages.keySet()) {
            if (!Pattern.matches(advantage, this.password)) {
                advices.add(advantages.get(advantage));
            }
        }
        for (String disadvantage : disadvantages.keySet()) {
            if (Pattern.matches(disadvantage, this.password)) {
                advices.add(disadvantages.get(disadvantage));
            }
        }
        return advices.toArray(new String[0]);
    }

}