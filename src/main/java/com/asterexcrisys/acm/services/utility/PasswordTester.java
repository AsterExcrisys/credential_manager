package com.asterexcrisys.acm.services.utility;

import com.asterexcrisys.acm.types.utility.PasswordStrength;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class PasswordTester {

    private static final HashMap<String, String> REQUIREMENTS = new HashMap<>();
    private static final HashMap<String, String> ADVANTAGES = new HashMap<>();
    private static final HashMap<String, String> DISADVANTAGES = new HashMap<>();
    private final String password;

    static {
        REQUIREMENTS.put("^[a-zA-Z0-9\\-+.*^$;,!£%&=@#_<>]+$", "Must contain at least one valid character");
        ADVANTAGES.put("^(.*[a-z].*){5,}$", "Should have five or more lower-case letters");
        ADVANTAGES.put("^(.*[A-Z].*)+$", "Should have one or more upper-case letters");
        ADVANTAGES.put("^(.*[0-9].*){3,}$", "Should have three or more decimal numbers");
        ADVANTAGES.put("^(.*[\\-+.*^$;,!£%&=@#_<>].*){1,}$", "Should have one or more special characters");
        ADVANTAGES.put("^.{12,}$", "Should have twelve or more total characters");
        DISADVANTAGES.put("^.*[a-zA-Z]{8,}.*$", "Should not have eight or more consecutive lower/upper-case letters");
        DISADVANTAGES.put("^.*[0-9]{6,}.*$", "Should not have six or more consecutive decimal numbers");
        DISADVANTAGES.put("^.*[\\-+.*^$;,!£%&=@#_<>]{4,}.*$", "Should not have four or more consecutive special characters");
        DISADVANTAGES.put("^.{1,6}$", "Should not have six or less total characters");
    }
    
    public PasswordTester(String password) throws NullPointerException {
        this.password = Objects.requireNonNull(password);
    }

    public boolean isPasswordValid() {
        for (String requirement : REQUIREMENTS.keySet()) {
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
        for (String advantage : ADVANTAGES.keySet()) {
            if (Pattern.matches(advantage, password)) {
                score += 100F / ADVANTAGES.size();
            }
        }
        for (String disadvantage : DISADVANTAGES.keySet()) {
            if (Pattern.matches(disadvantage, password)) {
                score -= 100F / DISADVANTAGES.size();
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
        for (String advantage : ADVANTAGES.keySet()) {
            if (!Pattern.matches(advantage, password)) {
                advices.add(ADVANTAGES.get(advantage));
            }
        }
        for (String disadvantage : DISADVANTAGES.keySet()) {
            if (Pattern.matches(disadvantage, password)) {
                advices.add(DISADVANTAGES.get(disadvantage));
            }
        }
        return advices.toArray(new String[0]);
    }

}