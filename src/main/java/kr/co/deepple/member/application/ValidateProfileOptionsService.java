package kr.co.deepple.member.application;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.co.deepple.member.application.port.in.ProfileOptions;
import kr.co.deepple.member.application.port.in.ValidateProfileOptionsUseCase;
import kr.co.deepple.member.domain.City;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;
import org.springframework.stereotype.Service;

@Service
public class ValidateProfileOptionsService implements ValidateProfileOptionsUseCase {

  @Override
  public ProfileOptions validate(ProfileOptions options) {
    Set<String> hobbies = normalize(options.hobbies(), Hobby::from);
    Set<String> cities = normalize(options.cities(), City::from);
    String religion = normalize(options.religion(), Religion::from);
    String smokingStatus = normalize(options.smokingStatus(), SmokingStatus::from);
    String drinkingStatus = normalize(options.drinkingStatus(), DrinkingStatus::from);
    return new ProfileOptions(cities, hobbies, religion, smokingStatus, drinkingStatus);
  }

  private static <E extends Enum<E>> Set<String> normalize(
      Set<String> values, Function<String, E> parser) {
    if (values == null) {
      return null;
    }
    return values.stream().map(value -> normalize(value, parser)).collect(Collectors.toSet());
  }

  private static <E extends Enum<E>> String normalize(String value, Function<String, E> parser) {
    E parsed = parser.apply(value);
    return parsed == null ? null : parsed.name();
  }
}
