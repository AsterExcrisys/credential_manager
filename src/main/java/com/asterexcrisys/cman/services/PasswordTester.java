package com.asterexcrisys.cman.services;

import com.asterexcrisys.cman.types.PasswordStrength;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class PasswordTester {

    private static final HashMap<String, String> requirements = new HashMap<>();
    private static final HashMap<String, String> advantages = new HashMap<>();
    private static final HashMap<String, String> disadvantages = new HashMap<>();
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
            if (!Pattern.matches(requirement, password)) {
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
            if (Pattern.matches(advantage, password)) {
                score += 100F / advantages.size();
            }
        }
        for (String disadvantage : disadvantages.keySet()) {
            if (Pattern.matches(disadvantage, password)) {
                score -= 100F / disadvantages.size();
            }
        }
        return score <= 100F? Math.max(score, 0F):100F;
    }

    public PasswordStrength getStrengthGrade() {
        float score = this.getStrengthScore();
        if (score >= 80F && score <= 100F) {
            return PasswordStrength.EXCELLENT;
        } else if (score >= 60F && score <= 80F) {
            return PasswordStrength.GOOD;
        } else if (score >= 40F && score <= 60F) {
            return PasswordStrength.DECENT;
        } else if (score >= 20F && score <= 40F) {
            return PasswordStrength.BAD;
        } else {
            return PasswordStrength.TERRIBLE;
        }
    }

    public String[] getSafetyAdvices() {
        ArrayList<String> advices = new ArrayList<>(0);
        for (String advantage : advantages.keySet()) {
            if (!Pattern.matches(advantage, password)) {
                advices.add(advantages.get(advantage));
            }
        }
        for (String disadvantage : disadvantages.keySet()) {
            if (Pattern.matches(disadvantage, password)) {
                advices.add(disadvantages.get(disadvantage));
            }
        }
        return advices.toArray(new String[0]);
    }

}